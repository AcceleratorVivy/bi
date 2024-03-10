package com.mika.bi.manager;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.mika.bi.config.RabbitMQConfig.BI_EXCHANGE;
import static com.mika.bi.config.RabbitMQConfig.BI_TOPIC_ROUTINGKEY;

@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(BI_EXCHANGE,BI_TOPIC_ROUTINGKEY,message);
    }
}
