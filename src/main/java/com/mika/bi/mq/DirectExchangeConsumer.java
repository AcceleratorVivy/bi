package com.mika.bi.mq;

import com.rabbitmq.client.*;

public class DirectExchangeConsumer {

    private static final String EXCHANGE_NAME = "direct_exchange";
    private static final String QUEUE1 = "direct_queue1";
    private static final String QUEUE2 = "direct_queue2";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    channel.queueDeclare(QUEUE1,true,false,false,null);
    channel.queueDeclare(QUEUE2,true,false,false,null);
    channel.queueBind(QUEUE1,EXCHANGE_NAME,"queue1");
    channel.queueBind(QUEUE2,EXCHANGE_NAME,"queue2");

    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [ queue1 ] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
      DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [ queue1 ] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
    channel.basicConsume(QUEUE1, true, deliverCallback1, consumerTag -> { });
    channel.basicConsume(QUEUE2, true, deliverCallback2, consumerTag -> { });
  }
}