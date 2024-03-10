package com.mika.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicExchangeConsumer {

    private static final String EXCHANGE_NAME = "topic_exchange";
    private static final String QUEUE1 = "topic_queue1";
    private static final String QUEUE2 = "topic_queue2";
    private static final String QUEUE3 = "topic_queue3";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    channel.queueDeclare(QUEUE1,true,false,false,null);
    channel.queueDeclare(QUEUE2,true,false,false,null);
    channel.queueDeclare(QUEUE3,true,false,false,null);
    channel.queueBind(QUEUE1,EXCHANGE_NAME,"#.q1.#");
    channel.queueBind(QUEUE2,EXCHANGE_NAME,"#.q2.#");
    channel.queueBind(QUEUE3,EXCHANGE_NAME,"#.q3.#");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [ queue1 ] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
      DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [ queue2 ] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
      DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [ queue3 ] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
    channel.basicConsume(QUEUE1, true, deliverCallback, consumerTag -> { });
    channel.basicConsume(QUEUE2, true, deliverCallback2, consumerTag -> { });
    channel.basicConsume(QUEUE3, true, deliverCallback3, consumerTag -> { });
  }
}