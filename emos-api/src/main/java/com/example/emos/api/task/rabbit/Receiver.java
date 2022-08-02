package com.example.emos.api.task.rabbit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.Constants;
import com.example.emos.api.common.util.RedisCache;
import com.example.emos.api.config.RabbitMQConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//@Slf4j
//@Component
public class Receiver {
/*

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisCache redisCache;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConfig.TO_WORKFLOW_QUEUE),
            exchange = @Exchange(value = RabbitMQConfig.TO_WORKFLOW_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = {RabbitMQConfig.TO_WORKFLOW_RK}
    ))
    public void retry(Message message, Channel channel) throws IOException, InterruptedException {
        String msg = (String) message.getPayload();
        MessageHeaders msh = message.getHeaders();

        String correlationId = (String) msh.get(AmqpHeaders.CORRELATION_ID);
        // 制造异常
        try {

            // 幂等
            if(StrUtil.isNotBlank(correlationId)) {
                if (redisCache.getCacheObject(Constants.RABBIT_CONSUMER_MESSAGE_ID + correlationId) != null) {
                    log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") + " 消息correlationId: " + correlationId + " 重复消费");
                    return;
                }
                else {
                    redisCache.setCacheObject(Constants.RABBIT_CONSUMER_MESSAGE_ID + correlationId, -1, 30, TimeUnit.MINUTES);
                }
            }

            // 业务逻辑
            System.out.println("=============================================");
            System.out.println("参数：" + JSONUtil.parse(msg).toBean(HashMap.class));
            System.out.println("方法：" + msh.get(Constants.RABBIT_TARGET));
            System.out.println("重发次数：" + msh.get(Constants.RABBIT_CONSUMER_RETRY_NUM));
            System.out.println("=============================================");


        } catch (Exception e) {
            int retryCount = (int) msh.get(Constants.RABBIT_CONSUMER_RETRY_NUM);
            CorrelationData correlationData = new CorrelationData(correlationId);
            Message newMessage = builderMessage(msg, msh, retryCount);
            // 重发 3 次还未成功，就发送到失败队列
            if (retryCount >= 3) {
                rabbitTemplate.convertAndSend(RabbitMQConfig.CUSTOMER_FAILED_EXCHANGE, RabbitMQConfig.CUSTOMER_FAILED_RK,
                        newMessage, correlationData);
                log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") + " 消费者最终消费失败，消息发送到失败队列;");
            } else {
                // 重试 3 次，每次相隔 8 秒，发送到重试队列
                redisCache.deleteObject(Constants.RABBIT_CONSUMER_MESSAGE_ID + correlationId);
                rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_CUSTOMER_RETRY_EXCHANGE, RabbitMQConfig.DELAY_CUSTOMER_RETRY_RK,
                        newMessage, correlationData);
                log.error("时间：" + DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss") + " 工作流消费者消费失败，消息发送到重试队列;"  + "第" + (retryCount+1) + "次重试");
            }

        } finally {
            long deliveryTag = (long) msh.get(AmqpHeaders.DELIVERY_TAG);
            channel.basicAck(deliveryTag, false);
        }

    }

    private Message builderMessage(Object msg, MessageHeaders msh, int retryCount) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AmqpHeaders.CORRELATION_ID, msh.get(AmqpHeaders.CORRELATION_ID));
        properties.put(Constants.RABBIT_CONSUMER_RETRY_NUM, ++retryCount);
        properties.put(Constants.RABBIT_TARGET, msh.get(Constants.RABBIT_TARGET));
        MessageHeaders headers = new MessageHeaders(properties);

        Message message = MessageBuilder.createMessage(msg, headers);

        return message;
    }*/
}