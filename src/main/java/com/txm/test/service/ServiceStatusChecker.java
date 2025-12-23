package com.txm.test.service;

import com.txm.test.entity.ServiceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ServiceStatusChecker {

    private static final Logger logger = LoggerFactory.getLogger(ServiceStatusChecker.class);

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    /**
     * 定时检查各服务状态，每30秒执行一次
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void checkServiceStatus() {
        logger.info("开始检查各服务连接状态...");

        CompletableFuture<Void> redisCheck = CompletableFuture.runAsync(this::checkRedisStatus);
        CompletableFuture<Void> mongoCheck = CompletableFuture.runAsync(this::checkMongoStatus);
        CompletableFuture<Void> mysqlCheck = CompletableFuture.runAsync(this::checkMysqlStatus);
        CompletableFuture<Void> rabbitCheck = CompletableFuture.runAsync(this::checkRabbitStatus);

        CompletableFuture.allOf(redisCheck, mongoCheck, mysqlCheck, rabbitCheck)
                .thenRun(() -> logger.info("所有服务状态检查完成"))
                .exceptionally(ex -> {
                    logger.error("服务状态检查过程中出现异常", ex);
                    return null;
                });
    }

    private void checkRedisStatus() {
        try {
            if (redisConnectionFactory != null) {
                // 尝试获取连接来验证Redis是否可达
                var connection = redisConnectionFactory.getConnection();
                connection.ping();
                connection.close();
                logger.info("Redis服务状态: 正常");
                saveServiceLog("Redis", "正常", "Redis服务状态正常");
            } else {
                logger.warn("Redis服务状态: 未配置");
                saveServiceLog("Redis", "未配置", "Redis服务未配置");
            }
        } catch (Exception e) {
            logger.error("Redis服务状态: 异常", e);
            saveServiceLog("Redis", "异常", "Redis服务状态异常: " + e.getMessage());
        }
    }

    private void checkMongoStatus() {
        try {
            if (mongoTemplate != null) {
                org.bson.Document result = mongoTemplate.executeCommand("{ ping: 1 }");
                
                if (result == null) {
                    logger.info("MongoDB服务状态: PING无响应");
                    saveServiceLog("MongoDB", "PING无响应", "MongoDB服务状态: PING无响应");
                }
                if (result != null && result.containsKey("ok")) {
                    Object okValue = result.get("ok");
                    if (okValue instanceof Number && ((Number) okValue).intValue() == 1) {
                        logger.info("MongoDB服务状态: 正常");
                        saveServiceLog("MongoDB", "正常", "MongoDB服务状态正常");
                    } else {
                        logger.info("MongoDB服务状态: 下线");
                        saveServiceLog("MongoDB", "下线", "MongoDB服务状态下线");
                    }
                } else {
                    logger.info("MongoDB服务状态: 正常");
                    saveServiceLog("MongoDB", "正常", "MongoDB服务状态正常");
                }
            } else {
                logger.warn("MongoDB服务状态: 未配置");
                saveServiceLog("MongoDB", "未配置", "MongoDB服务未配置");
            }
        } catch (Exception e) {
            logger.error("MongoDB服务状态: 异常", e);
            saveServiceLog("MongoDB", "异常", "MongoDB服务状态异常: " + e.getMessage());
        }
    }

    private void checkMysqlStatus() {
        try {
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(5)) {
                        logger.info("MySQL服务状态: 正常");
                        saveServiceLog("MySQL", "正常", "MySQL服务状态正常");
                    } else {
                        logger.error("MySQL服务状态: 连接无效");
                        saveServiceLog("MySQL", "连接无效", "MySQL服务状态连接无效");
                    }
                }
            } else {
                logger.warn("MySQL服务状态: 未配置");
                saveServiceLog("MySQL", "未配置", "MySQL服务未配置");
            }
        } catch (SQLException e) {
            logger.error("MySQL服务状态: 异常", e);
            saveServiceLog("MySQL", "异常", "MySQL服务状态异常: " + e.getMessage());
        }
    }

    private void checkRabbitStatus() {
        try {
            if (rabbitTemplate != null) {
                // 使用更简单的方式检查RabbitMQ连接
                rabbitTemplate.execute(channel -> {
                    // 简单地检查连接是否活跃
                    return channel.getConnection().isOpen();
                });
                logger.info("RabbitMQ服务状态: 正常");
                saveServiceLog("RabbitMQ", "正常", "RabbitMQ服务状态正常");
            } else {
                logger.warn("RabbitMQ服务状态: 未配置");
                saveServiceLog("RabbitMQ", "未配置", "RabbitMQ服务未配置");
            }
        } catch (Exception e) {
            logger.error("RabbitMQ服务状态: 异常", e);
            saveServiceLog("RabbitMQ", "异常", "RabbitMQ服务状态异常: " + e.getMessage());
        }
    }

    private void saveServiceLog(String serviceName, String status, String message) {
        if (mongoTemplate != null) {
            ServiceLog log = new ServiceLog(serviceName, status, message);
            mongoTemplate.save(log);
        }
    }
}