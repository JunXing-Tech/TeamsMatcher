package tech.jxing.teams_matcher.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * @@ConfigurationProperties 读取配置文件中的配置参数
 *
 * @author JunXing
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    /**
     * 从配置文件中获取Redis地址
     */
    private String host;
    private String port;

    @Bean
    public RedissonClient redissonClient() {
        // 创建配置
        Config config = new Config();
        // 获取Redis地址
        String redisAddress = String.format("redis://%s:%s", host, port);
        /**
         * 配置Redis地址和数据库
         * config.useSingleServer() 使用单机模式
         * config.useClusterServers() 使用集群模式
         */
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);
        // 创建RedissonClient对象
        RedissonClient redissonClient = Redisson.create(config);
        // 返回RedissonClient对象
        return redissonClient;
    }
}
