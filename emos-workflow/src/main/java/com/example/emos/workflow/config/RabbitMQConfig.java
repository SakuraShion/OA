package com.example.emos.workflow.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created By zf
 * 描述:
 */
@Configuration
public class RabbitMQConfig {

    // 工作流相关业务 RabbitMQ 队列、交换机、路由
    public static final String TO_WORKFLOW_QUEUE = "workflow.queue";
    public static final String TO_WORKFLOW_RK = "workflow.key";
    public static final String TO_WORKFLOW_EXCHANGE = "workflow.exchange";

    // 消费者延迟队列，死信队列，key
    public static final String DEAD_LETTER_EXCHANGE_KEY = "x-dead-letter-exchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
    public static final String X_MESSAGE_TTL = "x-message-ttl";

    // 死信队列信息, 通过死信队列将消息设置一定存活时间，
    // 消费者延迟队列
    public static final String DELAY_CUSTOMER_RETRY_QUEUE = "delay.customer.retry.queue";
    // 消费者延迟交换机
    public static final String DELAY_CUSTOMER_RETRY_EXCHANGE = "delay.customer.retry.exchange";
    // 消费者延迟队列路由
    public static final String DELAY_CUSTOMER_RETRY_RK = "delay.customer.retry.rk";

    // 生产者延迟队列
    public static final String DELAY_PRODUCER_RETRY_QUEUE = "delay.producer.retry.queue";
    // 生产者延迟路由
    public static final String DELAY_PRODUCER_RETRY_RK = "delay.producer.retry.rk";
    // 生产者延迟交换机
    public static final String DELAY_PRODUCER_RETRY_EXCHANGE = "delay.producer.retry.exchange";

    // 生产者发送最终失败队列、交换机、路由
    public static final String PRODUCER_FAILED_QUEUE = "producer.failed.queue";
    public static final String PRODUCER_FAILED_EXCHANGE = "producer.failed.exchange";
    public static final String PRODUCER_FAILED_RK = "producer.failed.rk";

    // 消费者发送最终失败队列、交换机、路由
    public static final String CUSTOMER_FAILED_QUEUE = "customer.failed.queue";
    public static final String CUSTOMER_FAILED_EXCHANGE = "customer.failed.exchange";
    public static final String CUSTOMER_FAILED_RK = "customer.failed.rk";

    // 生产者延迟投递监听
    public static final String DELAY_PRODUCER_LISTEN_QUEUE = "delay.producer.listen.queue";
    public static final String DELAY_PRODUCER_LISTEN_EXCHANGE = "delay.producer.listen.exchange";
    public static final String DELAY_PRODUCER_LISTEN_RK = "delay.producer.listen.rk";


    @Bean
    public ConnectionFactory getFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);

        return factory;
    }


    /**
     * ================================================ 交换机 =========================================================
     **/
    // 工作流业务处理交换机
    @Bean(TO_WORKFLOW_EXCHANGE)
    public Exchange directExchange() {
        return ExchangeBuilder.directExchange(TO_WORKFLOW_EXCHANGE).durable(true).build();
    }

    // 消费者重试
    @Bean(DELAY_CUSTOMER_RETRY_EXCHANGE)
    public Exchange retryDirectExchange() {
        return ExchangeBuilder.directExchange(DELAY_CUSTOMER_RETRY_EXCHANGE).durable(true).build();
    }

    // 生产者延迟
    @Bean(DELAY_PRODUCER_RETRY_EXCHANGE)
    public Exchange delayProducerDirectExchange() {
        return ExchangeBuilder.directExchange(DELAY_PRODUCER_RETRY_EXCHANGE).durable(true).build();
    }

    @Bean(DELAY_PRODUCER_LISTEN_EXCHANGE)
    public Exchange delayProducerListenExchange() {
        return ExchangeBuilder.directExchange(DELAY_PRODUCER_LISTEN_EXCHANGE).durable(true).build();
    }

    // 消费者失败交换机
    @Bean(CUSTOMER_FAILED_EXCHANGE)
    public Exchange failedDirectExchange() {
        return ExchangeBuilder.directExchange(CUSTOMER_FAILED_EXCHANGE).durable(true).build();
    }

    // 生产者失败交换机
    @Bean(PRODUCER_FAILED_EXCHANGE)
    public Exchange failedProducerDirectExchange() {
        return ExchangeBuilder.directExchange(PRODUCER_FAILED_EXCHANGE).durable(true).build();
    }


    /**
     * ================================================ 队列 =========================================================
     **/

    // 工作流业务处理队列
    @Bean(TO_WORKFLOW_QUEUE)
    public Queue toWorkflow() {
        return QueueBuilder.durable(TO_WORKFLOW_QUEUE).build();
    }

    @Bean(DELAY_PRODUCER_LISTEN_QUEUE)
    public Queue delayProducerListenQueue() {
        return QueueBuilder.durable(DELAY_PRODUCER_LISTEN_QUEUE).build();
    }

    // 消费者延迟队列实现
    @Bean(DELAY_CUSTOMER_RETRY_QUEUE)
    public Queue customerRetryDirectQueue() {
        Map<String, Object> args = new ConcurrentHashMap<>(3);
        args.put(DEAD_LETTER_EXCHANGE_KEY, TO_WORKFLOW_EXCHANGE);
        args.put(DEAD_LETTER_ROUTING_KEY, TO_WORKFLOW_RK);
        args.put(X_MESSAGE_TTL, 8 * 1000);
        return QueueBuilder.durable(DELAY_CUSTOMER_RETRY_QUEUE).withArguments(args).build();
    }

    @Bean(DELAY_PRODUCER_RETRY_QUEUE)
    public Queue producerDelayDirectQueue() {
        Map<String, Object> args = new ConcurrentHashMap<>(3);
        args.put(DEAD_LETTER_EXCHANGE_KEY, DELAY_PRODUCER_LISTEN_EXCHANGE);
        args.put(DEAD_LETTER_ROUTING_KEY, DELAY_PRODUCER_LISTEN_RK);
        args.put(X_MESSAGE_TTL, 30 * 1000);
        return QueueBuilder.durable(DELAY_PRODUCER_RETRY_QUEUE).withArguments(args).build();
    }

    @Bean(CUSTOMER_FAILED_QUEUE)
    public Queue customerFailedDirectQueue() {
        return QueueBuilder.durable(CUSTOMER_FAILED_QUEUE).build();
    }

    @Bean(PRODUCER_FAILED_QUEUE)
    public Queue producerFailedDirectQueue() {
        return QueueBuilder.durable(PRODUCER_FAILED_QUEUE).build();
    }


    /**
     * ================================================ 绑定 =========================================================
     **/

    @Bean
    public Binding customerRetryDirectBinding(@Qualifier(DELAY_CUSTOMER_RETRY_QUEUE) Queue queue,
                                              @Qualifier(DELAY_CUSTOMER_RETRY_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_CUSTOMER_RETRY_RK).noargs();
    }

    @Bean
    public Binding producerRetryDirectBinding(@Qualifier(DELAY_PRODUCER_RETRY_QUEUE) Queue queue,
                                              @Qualifier(DELAY_PRODUCER_RETRY_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_PRODUCER_RETRY_RK).noargs();
    }

    @Bean
    public Binding producerListenBinding(@Qualifier(DELAY_PRODUCER_LISTEN_QUEUE) Queue queue,
                                         @Qualifier(DELAY_PRODUCER_LISTEN_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_PRODUCER_LISTEN_RK).noargs();
    }

    @Bean
    public Binding workDirectBinding(@Qualifier(TO_WORKFLOW_QUEUE) Queue queue,
                                     @Qualifier(TO_WORKFLOW_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TO_WORKFLOW_RK).noargs();
    }

    @Bean
    public Binding customerFailedDirectBinding(@Qualifier(CUSTOMER_FAILED_QUEUE) Queue queue,
                                               @Qualifier(CUSTOMER_FAILED_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(CUSTOMER_FAILED_RK).noargs();
    }

    @Bean
    public Binding producerFailedDirectBinding(@Qualifier(PRODUCER_FAILED_QUEUE) Queue queue,
                                               @Qualifier(PRODUCER_FAILED_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PRODUCER_FAILED_RK).noargs();
    }


    @Bean
    public MessagePostProcessor correlationIdProcessor() {
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                return message;
            }

            @Override
            public Message postProcessMessage(Message message, Correlation correlation) {
                MessageProperties properties = message.getMessageProperties();

                if (correlation instanceof CorrelationData) {
                    String correlationId = ((CorrelationData) correlation).getId();
                    properties.setCorrelationId(correlationId);
                }

                return message;
            }
        };

        return messagePostProcessor;
    }

}
