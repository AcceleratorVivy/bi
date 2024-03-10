package com.mika.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {

  private static final String TASK_QUEUE_NAME = "multi_queue";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();
     for(int i =0 ; i < 2; i++){
         final Channel channel = connection.createChannel();
         channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

         int finalI = i;
         DeliverCallback deliverCallback = (consumerTag, delivery) -> {
             String message = new String(delivery.getBody(), "UTF-8");

             try {
                 System.out.println(" [ 消费者 "+ finalI +" ] Received '" + message + "'");
                 channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
             } catch (Exception e) {
                 e.printStackTrace();
                 channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
             }finally {

             }

         };

         channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
     }
  }



}