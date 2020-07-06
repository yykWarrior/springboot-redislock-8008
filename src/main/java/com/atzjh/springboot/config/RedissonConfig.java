package com.atzjh.springboot.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * redission的配置
 *
 * @author zhangjh
 * @date2020-03-01
 */
@Component
public class RedissonConfig {

    @Autowired
    private RedisClusterProperties properties;

    /**
     * 获取redisson对象的方法,单机版
     * @return
     */
//    @Bean
//    public Redisson getRedisson() {
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis//:192.168.100.102:6379").setDatabase(0);
//        return (Redisson) Redisson.create(config);
//    }

    /**
     * 获取redisson对象的方法,集群版
     *
     * @return
     */
    @Bean
    public Redisson getRedisson() {
        String type = "redis://";
        String[] arr = properties.getClusterNodes().split(",");
        Config config = new Config();
        config.useClusterServers().addNodeAddress(type + arr[0]).addNodeAddress(type + arr[1]).addNodeAddress(type + arr[2])
                .addNodeAddress(type + arr[3]).addNodeAddress(type + arr[4]).addNodeAddress(type + arr[5]);
        return (Redisson) Redisson.create(config);
    }


}
