package com.atzjh.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * redisCluster
 *
 * @author zhangjh
 * @date 202-03-01
 */
@Component
@ConfigurationProperties(prefix = "redis.cache")
public class RedisClusterProperties {

    private String clusterNodes;
    private int connectionTimeout;
    private int soTimeout;
    private int maxAttempts;
    private String password;
    private int expireSeconds;

    private int poolMaxWait;
    private int poolMaxIdle;
    private int poolMaxActive;
    private int poolMaxTotal;
    private boolean poolTestOnBorrow;
    private int poolMinEvictableIdleTimeMillis;
    private int poolTimeBetweenEvictionRunsMillis;

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public int getPoolMaxWait() {
        return poolMaxWait;
    }

    public void setPoolMaxWait(int poolMaxWait) {
        this.poolMaxWait = poolMaxWait;
    }

    public int getPoolMaxIdle() {
        return poolMaxIdle;
    }

    public void setPoolMaxIdle(int poolMaxIdle) {
        this.poolMaxIdle = poolMaxIdle;
    }

    public int getPoolMaxActive() {
        return poolMaxActive;
    }

    public void setPoolMaxActive(int poolMaxActive) {
        this.poolMaxActive = poolMaxActive;
    }

    public int getPoolMaxTotal() {
        return poolMaxTotal;
    }

    public void setPoolMaxTotal(int poolMaxTotal) {
        this.poolMaxTotal = poolMaxTotal;
    }

    public boolean isPoolTestOnBorrow() {
        return poolTestOnBorrow;
    }

    public void setPoolTestOnBorrow(boolean poolTestOnBorrow) {
        this.poolTestOnBorrow = poolTestOnBorrow;
    }

    public int getPoolMinEvictableIdleTimeMillis() {
        return poolMinEvictableIdleTimeMillis;
    }

    public void setPoolMinEvictableIdleTimeMillis(int poolMinEvictableIdleTimeMillis) {
        this.poolMinEvictableIdleTimeMillis = poolMinEvictableIdleTimeMillis;
    }

    public int getPoolTimeBetweenEvictionRunsMillis() {
        return poolTimeBetweenEvictionRunsMillis;
    }

    public void setPoolTimeBetweenEvictionRunsMillis(int poolTimeBetweenEvictionRunsMillis) {
        this.poolTimeBetweenEvictionRunsMillis = poolTimeBetweenEvictionRunsMillis;
    }

}
