package com.mika.bi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // 定义死信交换机的名称
    public static final String DEAD_LETTER_EXCHANGE = "dead_letter_exchange";

    // 定义死信队列的名称
    public static final String DEAD_LETTER_QUEUE = "dead_letter_queue";

    // 定义原始队列的名称
    public static final String BI_QUEUE = "bi_queue";

    // 定义原始交换机的名称
    public static final String BI_EXCHANGE = "bi_exchange";

    public static final String BI_TOPIC_ROUTINGKEY = "#.genChart.#";

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE,true,false,null);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE,true,false,false);
    }

    @Bean
    public Binding bindingDeadLetterQueueToExchange() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(BI_TOPIC_ROUTINGKEY);
    }

    @Bean
    public TopicExchange originalExchange() {
        return new TopicExchange(BI_EXCHANGE,true,false,null);
    }
    @Bean
    public Queue originalQueue() {
        // 设置队列参数，指定死信交换机和路由键
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", BI_TOPIC_ROUTINGKEY);
        return new Queue(BI_QUEUE, true, false, false, args);
    }
    @Bean
    public Binding bindingOriginalQueueToExchange() {
        return BindingBuilder.bind(originalQueue()).to(originalExchange()).with(BI_TOPIC_ROUTINGKEY);
    }







}
