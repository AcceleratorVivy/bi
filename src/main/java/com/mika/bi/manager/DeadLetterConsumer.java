package com.mika.bi.manager;

import com.mika.bi.exception.BusinessException;
import com.mika.bi.model.entity.Chart;
import com.mika.bi.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.mika.bi.config.RabbitMQConfig.DEAD_LETTER_QUEUE;

@Component
@Slf4j
public class DeadLetterConsumer {

    @Resource
    private ChartService chartService;

    @RabbitListener(queues = {DEAD_LETTER_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("失败的图表生成 : {}", message);
            long id = Long.parseLong(message);
            Chart chart = chartService.getById(id);
            if(chart == null) {
                throw new Exception("图表不存在");
            }
            chart.setStatus("failed");
            chartService.updateById(chart);
            channel.basicReject(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                channel.basicReject(deliveryTag, false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
