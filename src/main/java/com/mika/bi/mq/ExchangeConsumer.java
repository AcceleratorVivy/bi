package com.mika.bi.mq;

import com.rabbitmq.client.*;

public class ExchangeConsumer {
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
    channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,"queue1");
    channel.queueBind(QUEUE_NAME2,EXCHANGE_NAME,"queue2");


    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [queue1 ] Received '" + message + "'");
    };
    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [queue2] Received '" + message + "'");
    };
    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    channel.basicConsume(QUEUE_NAME2, true, deliverCallback1, consumerTag -> { });
  }
}