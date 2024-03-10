package com.mika.bi.mq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class DDLConsumerForTTL {

    private final static String DDL_QUEUE = "DDL_queue";
    private final static String DDL_QUEUE2 = "DDL_queue1";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(DDL_QUEUE, true, false, false, null);
        channel.queueDeclare(DDL_QUEUE2, true, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println("[ "+ LocalDateTime.now()+"  DDL_CONSUMER ] :" + new String(message.getBody(), StandardCharsets.UTF_8) +" ::: "+ message.getEnvelope().getRoutingKey());
        };
        DeliverCallback deliverCallback1 = (consumerTag, message) -> {
            System.out.println("[ "+ LocalDateTime.now()+"  DDL_CONSUMER2 ] :" + new String(message.getBody(), StandardCharsets.UTF_8)+" ::: "+ message.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(DDL_QUEUE, true, deliverCallback, consumerTag -> {
        });
        channel.basicConsume(DDL_QUEUE2, true, deliverCallback1, consumerTag -> {
        });

    }
}