package tech.jxing.teams_matcher.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import tech.jxing.teams_matcher.model.domain.User;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author JunXing
 */

@SpringBootTest
public class RedissonTest implements Serializable {

    @Resource
    RedissonClient redissonClient;

    @Test
    public void stringTest() {
        /**
         * Redisson 字符串操作
         */
        RBucket<Object> rBucket = redissonClient.getBucket("strKey");
        rBucket.set("strValue", 30, TimeUnit.SECONDS);
        System.out.println(redissonClient.getBucket("strKey").get());
    }

    @Test
    public void objectTest() {
        /**
         * 对象操作
         */
        User user = new User();
        user.setId(1L);
        user.setUsername("objKey");

        RBucket<Object> rBucket  = redissonClient.getBucket("objKey");
        rBucket.set(user, 30, TimeUnit.SECONDS);
        System.out.println(redissonClient.getBucket("objKey").get());
    }

    @Test
    public void mapTest() {
        /**
         * map操作
         */
        RMap<String, String> rMap = redissonClient.getMap("mapKey");
        rMap.put("mapKey1", "mapValue1");
        rMap.put("mapKey2", "mapValue2");
        rMap.expire(30, TimeUnit.SECONDS);

        System.out.println(redissonClient.getMap("mapKey"));
    }

    @Test
    public void listTest() {
        /**
         * list操作
         */
        RList<Object> rList = redissonClient.getList("listKey");
        rList.add("listValue1");
        rList.add("listValue2");

        rList.expire(30, TimeUnit.SECONDS);
        System.out.println(redissonClient.getList("listKey"));
    }

    @Test
    public void setTest() {
        /**
         * set操作
         */
        RSet<Object> rSet = redissonClient.getSet("setKey");
        rSet.add("setValue1");
        rSet.add("setValue2");

        rSet.expire(30, TimeUnit.SECONDS);
        System.out.println(redissonClient.getSet("setKey"));
    }

    /**
     * 测试Redisson中分布式锁的自动续锁机制
     */
    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("teamsmatcher:precache:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(300000);
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
