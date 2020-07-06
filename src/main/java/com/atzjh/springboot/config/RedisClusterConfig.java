package com.atzjh.springboot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * redisCluster配置类
 *
 * @author zhangjh
 * @date 2020-03-01
 */
@Configuration
public class RedisClusterConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClusterConfig.class);

    @Autowired
    private RedisClusterProperties redisClusterProperties;

    /**
     * 配置 Redis 连接池信息
     */
    @Bean
    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(2000);
        //最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(500);
        // 最小空闲连接数, 默认0
//        jedisPoolConfig.setMinIdle(0);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(30000);
//        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
//        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(-1);
//        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
//        jedisPoolConfig.setMinEvictableIdleTimeMillis(18000);
//        // 在获取连接的时候检查有效性, 默认false
//        jedisPoolConfig.setTestOnBorrow(false);
//        // 在空闲时检查有效性, 默认false
//        jedisPoolConfig.setTestWhileIdle(false);
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(false);
        return jedisPoolConfig;
    }

    /**
     * redis集群配置
     *
     * @param jedisPoolConfig
     * @return
     */
    @Bean
    public RedisClusterConfiguration getJedisCluster(JedisPoolConfig jedisPoolConfig) {
        //所有的IP和端口 例如:127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002
        String[] serverArray = redisClusterProperties.getClusterNodes().split(",");

        Set<RedisNode> redisClusterNodes = new HashSet<RedisNode>();
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            redisClusterNodes.add(new RedisNode(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        // 跨集群执行命令时要遵循的最大重定向数量
        redisClusterConfiguration.setMaxRedirects(3);
        redisClusterConfiguration.setClusterNodes(redisClusterNodes);

        return redisClusterConfiguration;
    }

    /**
     * 配置 Redis 连接工厂
     */
    @Bean
    public JedisConnectionFactory getJedisConnectionFactory(RedisClusterConfiguration redisClusterConfiguration, JedisPoolConfig jedisPoolConfig) {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration, jedisPoolConfig);
        return jedisConnectionFactory;
    }

    /**
     * 设置数据存入redis 的序列化方式
     * redisTemplate序列化默认使用的jdkSerializeable
     * 存储二进制字节码，导致key会出现乱码，所以自定义序列化类
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}