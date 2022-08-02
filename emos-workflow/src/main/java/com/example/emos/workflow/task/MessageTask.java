package com.example.emos.workflow.task;

import com.example.emos.workflow.db.pojo.MessageEntity;
import com.example.emos.workflow.db.pojo.MessageRefEntity;
import com.example.emos.workflow.service.MessageService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By zf
 * 描述:
 */
@Component
@Slf4j
public class MessageTask {

    @Autowired
    private ConnectionFactory factory;

    @Autowired
    private MessageService messageService;

    @Async("AsyncTaskExecutor")
    public void sendAsync(String topic, MessageEntity entity) {
        send(topic, entity);
    }

    public void send(String topic, MessageEntity entity) {

        String id = entity.get_id();

        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();) {

            channel.queueDeclare(topic, true, false, false, null);

            HashMap map = new HashMap();
            map.put("messageId", id);

            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();
            channel.basicPublish("", topic, properties, entity.getMsg().getBytes());

            log.debug("消息发送成功");
        } catch (Exception e) {
            log.error("消息发送异常", e);
        }
    }


    public int receive(String topic) {
        // 记录接受到的消息数量
        int i = 0;

        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();) {

            channel.queueDeclare(topic, true, false, false, null);

            while (true) {
                GetResponse resp = channel.basicGet(topic, false);
                if(resp != null) {
                    AMQP.BasicProperties properties = resp.getProps();
                    Map<String, Object> map = properties.getHeaders();
                    String messageId = map.get("messageId").toString();

                    MessageRefEntity entity = new MessageRefEntity();
                    entity.setMessageId(messageId);
                    entity.setLastFlag(true);
                    entity.setReadFlag(false);
                    entity.setReceiverId(Integer.parseInt(topic));
                    messageService.insertRef(entity);

                    long deliveryTag = resp.getEnvelope().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                    i++;
                } else {
                    break;
                }
            }

            log.debug("接收消息完毕");
        } catch (Exception e) {
            log.error("消息接收异常", e);
        }

        return i;
    }

    @Async("AsyncTaskExecutor")
    public Integer receiveAsync(String topic) {
        return receive(topic);
    }

    public void deleteQueue(String topic) {
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();) {

            channel.queueDelete(topic);

            log.debug("删除队列成功");
        } catch (Exception e) {
            log.error("删除队列失败", e);
        }
    }

    @Async("AsyncTaskExecutor")
    public void deleteQueueAsync(String topic) {
        deleteQueue(topic);
    }
}
