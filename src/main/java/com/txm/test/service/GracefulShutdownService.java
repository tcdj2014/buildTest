package com.txm.test.service;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.SmartLifecycle;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GracefulShutdownService implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownService.class);

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;
    
    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private LettuceConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        logger.info("收到进程停止信号，准备优雅关闭...");
        shuttingDown.set(true);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 停止所有RabbitMQ监听器
        if (rabbitListenerEndpointRegistry.isRunning()) {
            rabbitListenerEndpointRegistry.stop(() -> {
                logger.info("所有RabbitMQ监听器已停止");
            });
        }

        // 关闭Redis连接工厂
        closeRedisConnectionFactory();
        
        // 关闭MongoDB连接
        closeMongoDbConnection();
        
        // 关闭数据库连接池
        closeDataSource();
        
        running.set(false);
        logger.info("消息处理、Redis连接、MongoDB连接和数据库连接关闭后允许进程停止");
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    public boolean isShuttingDown() {
        return shuttingDown.get();
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void closeRedisConnectionFactory() {
        if (redisConnectionFactory != null) {
            logger.info("开始关闭Redis连接工厂");
            try {
                redisConnectionFactory.destroy();
                logger.info("Redis连接工厂已关闭");
            } catch (Exception e) {
                logger.error("关闭Redis连接工厂时发生错误", e);
            }
        } else {
            logger.info("未配置Redis连接工厂，跳过Redis连接关闭");
        }
    }

    private void closeMongoDbConnection() {
        if (mongoTemplate != null) {
            logger.info("开始关闭MongoDB连接");
            try {
                // Spring Data MongoDB会在应用上下文关闭时自动处理连接关闭
                // 这里仅记录日志表示MongoDB连接将被关闭
                logger.info("MongoDB连接将在应用上下文关闭时自动关闭");
            } catch (Exception e) {
                logger.error("关闭MongoDB连接时发生错误", e);
            }
        } else {
            logger.info("未配置MongoDB连接，跳过MongoDB连接关闭");
        }
    }

    private void closeDataSource() {
        if (dataSource instanceof HikariDataSource) {
            logger.info("开始关闭HikariDataSource连接池");
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            try {
                hikariDataSource.close();
                logger.info("HikariDataSource连接池已关闭");
            } catch (Exception e) {
                logger.error("关闭HikariDataSource时发生错误", e);
            }
        } else if (dataSource != null) {
            logger.info("检测到DataSource但不是HikariDataSource类型，跳过特殊处理");
        } else {
            logger.info("未配置DataSource，跳过数据库连接关闭");
        }
    }
}