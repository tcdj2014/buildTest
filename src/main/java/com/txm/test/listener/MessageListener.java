package com.txm.test.listener;

import com.txm.test.service.GracefulShutdownService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private GracefulShutdownService gracefulShutdownService;

    @RabbitListener(queues = "test.queue")
    public void handleMessage(String message) throws InterruptedException {
        logger.info("接收到消息: {}", message);

        // 模拟处理消息的时间
        for (int i = 0; i < 10; i++) {
            // 检查是否正在关闭过程中，如果正在关闭则抛出异常中断处理
            if (gracefulShutdownService.isShuttingDown()) {
                logger.info("系统正在关闭，中断消息处理");
                return;
            }
            
            // 模拟处理时间（总共5秒）
            Thread.sleep(500);
        }
        
        logger.info("消息处理完成: {}", message);
    }
}