package tech.jxing.teams_matcher.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author JunXing
 */

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void testRedis() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 保存数据
        valueOperations.set("key", "value");
        // 获取数据
        Object jxing = valueOperations.get("key");
        // 修改数据
        valueOperations.set("key", "valueChange");
        // 删除数据
        redisTemplate.delete("key");
        valueOperations.getAndDelete("key");
    }
}
