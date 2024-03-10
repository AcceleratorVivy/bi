package com.mika.bi.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TTLForMessage {

    private final static String QUEUE_NAME = "TTL_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            Map<String, Object> args = new HashMap<>();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String message = scanner.nextLine();
                byte[] messageBodyBytes = message.getBytes();
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                        .expiration("6000")
                        .build();
                channel.basicPublish("", QUEUE_NAME, properties, messageBodyBytes);
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }

        }
    }
}