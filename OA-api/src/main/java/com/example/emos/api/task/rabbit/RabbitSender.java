package com.example.emos.api.task.rabbit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.Constants;
import com.example.emos.api.common.util.RedisCache;
import com.example.emos.api.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created By zf
 * 描述:
 */
@Component
@Slf4j
public class RabbitSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private MessagePostProcessor correlationIdProcessor;

    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if(ack) {
                redisCache.deleteObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + correlationData.getId());
            }
        }
    };

    final RabbitTemplate.ReturnsCallback returnsCallback =  new RabbitTemplate.ReturnsCallback() {

        @Override
        public void returnedMessage(ReturnedMessage returned) {
            redisCache.setCacheObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + returned.getMessage().getMessageProperties().getCorrelationId(),
                    3, 30, TimeUnit.MINUTES);
            log.info("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                    "消息 correlationId：" +  " 路由失败，将投递到失败队列");
        }
    };

    @Async("AsyncTaskExecutor")
    public void send(Object message, String target) throws Exception {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(IdUtil.simpleUUID());

        send(message, target, correlationData);
    }

    @Async("AsyncTaskExecutor")
    public void send(Object message, String target, String correlationId) throws Exception {
        CorrelationData correlationData = new CorrelationData(correlationId);

        send(message, target, correlationData);
    }

    public void send(Object message, String target, CorrelationData correlationData) throws Exception {
        MessageHeaders mhs = new MessageHeaders(builderProperties(target));
        Message msg = MessageBuilder.createMessage(message, mhs);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnsCallback(returnsCallback);

        /**
         * 投递消息时将消息 id 缓存，返回 ack 时将其删除，同时投递一条延时消息，
         * 等待一定的延时后被投递到延时队列，消费者查缓存这条消息是否存在，存在说明消息投递失败了，就再次投递，缓存的值就是消息投递次数
         * 同时会记录投递次数达到 3 次后还未成功就投递到最终失败队列
         */
        if (redisCache.getCacheObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + correlationData.getId()) == null) {
            redisCache.setCacheObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + correlationData.getId(), 0, 30, TimeUnit.MINUTES);
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.TO_WORKFLOW_EXCHANGE, RabbitMQConfig.TO_WORKFLOW_RK, msg, correlationIdProcessor, correlationData);

        // 投递到延时队列
        rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_PRODUCER_RETRY_EXCHANGE, RabbitMQConfig.DELAY_PRODUCER_RETRY_RK, msg, correlationIdProcessor, correlationData);
        log.info("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                "消息 correlationId：" + correlationData.getId() + " 已投递到延时队列");
    }

    private Map<String, Object> builderProperties(String target) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.RABBIT_TARGET, target);
        properties.put(Constants.RABBIT_CONSUMER_RETRY_NUM, 0);

        return properties;
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConfig.DELAY_PRODUCER_LISTEN_QUEUE),
            exchange = @Exchange(value = RabbitMQConfig.DELAY_PRODUCER_LISTEN_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = {RabbitMQConfig.DELAY_PRODUCER_LISTEN_RK}
    ))
    public void delay(Message message, Channel channel) throws IOException, InterruptedException {
        String msg = (String) message.getPayload();
        MessageHeaders msh = message.getHeaders();
        String correlationId = (String) msh.get(AmqpHeaders.CORRELATION_ID);

        try {
            if(StrUtil.isNotBlank(correlationId)) {
                if (redisCache.getCacheObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + correlationId) != null) {
                    int retryCount = redisCache.getCacheObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + correlationId);
                    log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                            "消息 correlationId: " + correlationId + " 未被 MQ 正确接收，正在重新投递 重试次数：: " + retryCount);

                    if (retryCount >= 3) {
                        // 投递到失败队列
                        Message newMessage = builderMessage(msg, msh);
                        rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCER_FAILED_EXCHANGE, RabbitMQConfig.PRODUCER_FAILED_RK,
                                newMessage, new CorrelationData(correlationId));
                        log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                                "消息 correlationId: " + correlationId + " 重试次数过多，将投递到失败队列");
                    } else {
                        redisCache.setCacheObject(Constants.RABBIT_PRODUCER_MESSAGE_ID + correlationId, ++retryCount, 30, TimeUnit.MINUTES);

                        send(msg, (String) msh.get(Constants.RABBIT_TARGET), correlationId);

                        log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                                "消息 correlationId: " + correlationId + " 已重新投递");
                    }

                }
                else {
                    log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                            "消息 correlationId: " + correlationId + " 消息被正确接受，延迟消息丢弃");
                }
            }

        } catch (Exception e) {
            Message newMessage = builderMessage(msg, msh);
            rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCER_FAILED_EXCHANGE, RabbitMQConfig.PRODUCER_FAILED_RK,
                    newMessage, new CorrelationData(correlationId));
            log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") +
                    "消息 correlationId: " + correlationId + " 消息异常");
        } finally {
            long deliveryTag = (long) msh.get(AmqpHeaders.DELIVERY_TAG);
            channel.basicAck(deliveryTag, false);
        }

    }

    private Message builderMessage(Object msg, MessageHeaders msh) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AmqpHeaders.CORRELATION_ID, msh.get(AmqpHeaders.CORRELATION_ID));
        properties.put(Constants.RABBIT_CONSUMER_RETRY_NUM, 0);
        properties.put(Constants.RABBIT_TARGET, msh.get(Constants.RABBIT_TARGET));
        MessageHeaders headers = new MessageHeaders(properties);

        Message message = MessageBuilder.createMessage(msg, headers);

        return message;
    }
}
