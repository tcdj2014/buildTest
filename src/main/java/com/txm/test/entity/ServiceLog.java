package com.txm.test.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "servicelog")
public class ServiceLog {
    
    @Id
    private String id;
    
    private String serviceName;
    
    private String status;
    
    private String message;
    
    private LocalDateTime checkTime;
    
    @Indexed(expireAfter = "30d") // 30天过期
    private LocalDateTime expireAt;
    
    // Constructors
    public ServiceLog() {}
    
    public ServiceLog(String serviceName, String status, String message) {
        this.serviceName = serviceName;
        this.status = status;
        this.message = message;
        this.checkTime = LocalDateTime.now();
        this.expireAt = LocalDateTime.now().plusDays(30); // 30天后过期
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getCheckTime() {
        return checkTime;
    }
    
    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }
    
    public LocalDateTime getExpireAt() {
        return expireAt;
    }
    
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}