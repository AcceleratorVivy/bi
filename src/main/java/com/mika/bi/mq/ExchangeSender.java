package com.mika.bi.mq;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class ExchangeSender {

    private static final String EXCHANGE_NAME = "first_exchange";
    private static final String QUEUE_NAME = "exchange_queue1";
    private static final String QUEUE_NAME2 = "exchange_queue2";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        channel.queueDeclare(QUEUE_NAME2,true,false,false,null);

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String s = scanner.nextLine();
            String[] message = s.split(" ");
            channel.basicPublish(EXCHANGE_NAME,message[1],
                    null,
                    message[0].getBytes("UTF-8"));
        }
    }

}
