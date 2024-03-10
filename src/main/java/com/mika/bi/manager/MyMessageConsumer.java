package com.mika.bi.manager;

import com.mika.bi.common.ErrorCode;
import com.mika.bi.exception.ThrowUtils;
import com.mika.bi.model.entity.Chart;
import com.mika.bi.service.ChartService;
import com.mika.bi.utils.ExcelUtils;
import com.rabbitmq.client.Channel;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.mika.bi.config.RabbitMQConfig.BI_QUEUE;

@Slf4j
@Component
public class MyMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * 接收消息的方法
     *
     * @param message     接收到的消息内容，是一个字符串类型
     * @param channel     消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息
     */
    // 使用@SneakyThrows注解简化异常处理
    // 使用@RabbitListener注解指定要监听的队列名称为"code_queue"，并设置消息的确认机制为手动确认
    @RabbitListener(queues = {BI_QUEUE}, ackMode = "MANUAL")
    // @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解,用于从消息头中获取投递标签(deliveryTag),
    // 在RabbitMQ中,每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序。通过使用@Header(AmqpHeaders.DELIVERY_TAG)注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 使用日志记录器打印接收到的消息内容
            log.info("receiveMessage message = {}", message);
            // 投递标签是一个数字标识,它在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态。通过将投递标签传递给channel.basicAck(deliveryTag, false)方法,可以告知RabbitMQ该消息已经成功处理,可以进行确认和从队列中删除。
            // 手动确认消息的接收，向RabbitMQ发送确认消息


            Chart chart = chartService.getById(Long.parseLong(message));
            String goal = chart.getGoal();
            String excelData = chart.getChartData();
            chart.setStatus("running");
            boolean b = chartService.updateById(chart);
            if(!b){
                chartService.genError(chart, ErrorCode.OPERATION_ERROR,"执行失败");
            }

            StringBuffer requestBuffer = new StringBuffer();
            requestBuffer.append("分析需求:\n");
            requestBuffer.append(goal).append("请使用:").append(chart.getChartType()).append("\n");
            requestBuffer.append("原始数据:\n").append(excelData);
            if(requestBuffer.length()>1024){
                chartService.genError(chart,ErrorCode.OPERATION_ERROR,"数据过大生成图表失败");
            }

            Long modelId = 1659171950288818178L;
            DevChatRequest devChatRequest = new DevChatRequest(modelId,String.valueOf(requestBuffer));
            com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);

            String[] data = response.getData().getContent().split("【【【【【");
            if(data.length != 3){
                chartService.genError(chart,ErrorCode.SYSTEM_ERROR,"AI 故障");
            }
            String chartCode = data[1];
            String result = data[2];
            chart.setGenChart(chartCode);
            chart.setGenResult(result);
            chart.setStatus("succeed");
            b = chartService.updateById(chart);
            ThrowUtils.throwIf(!b,ErrorCode.OPERATION_ERROR,"图表生成失败");
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
