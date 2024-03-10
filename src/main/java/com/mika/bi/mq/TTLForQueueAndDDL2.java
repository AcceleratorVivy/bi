package com.mika.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TTLForQueueAndDDL2 {

    private final static String QUEUE_NAME = "TTL_queue1";
    private final static String QUEUE_NAME2 = "TTL_queue2";
    private final static String DDL_EXCHANGE = "DDL_exchange";
    private final static String DDL_QUEUE = "DDL_queue";
    private final static String DDL_QUEUE2 = "DDL_queue1";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(DDL_QUEUE, true, false, false, null);
            channel.queueDeclare(DDL_QUEUE2, true, false, false, null);
            channel.exchangeDeclare(DDL_EXCHANGE, "direct");
            channel.queueBind(DDL_QUEUE, DDL_EXCHANGE, "ddl1");
            channel.queueBind(DDL_QUEUE2, DDL_EXCHANGE, "ddl2");

            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", 6000);
            args.put("x-dead-letter-exchange", DDL_EXCHANGE);
            args.put("x-dead-letter-routing-key", "ddl2");
            channel.queueDeclare(QUEUE_NAME2, false, false, false, args);


            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                byte[] messageBodyBytes = message.getBytes();
                channel.basicPublish("", QUEUE_NAME2, null, messageBodyBytes);
                System.out.println("[" + LocalDateTime.now() + "] [x] Sent '" + message + "'");


            }

        }
    }
}