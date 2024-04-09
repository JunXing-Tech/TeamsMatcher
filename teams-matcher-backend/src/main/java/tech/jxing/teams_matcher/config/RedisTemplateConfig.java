package tech.jxing.teams_matcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author JunXing
 */
@Configuration
public class RedisTemplateConfig {

    /**
     * 创建并配置RedisTemplate Bean，用于操作Redis数据库。
     *
     * @param redisConnectionFactory Redis连接工厂，用于创建连接到Redis服务器的连接。
     * @return 配置好的RedisTemplate实例，可以用于执行Redis操作。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置Redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置键的序列化方式为字符串序列化
        redisTemplate.setKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }
}
