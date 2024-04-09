package tech.jxing.teams_matcher.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.jxing.teams_matcher.model.domain.User;
import tech.jxing.teams_matcher.service.UserService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author JunXing
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 主要用户列表，此处示例只有两个用户ID，实际应用中可根据需求调整
     */
    private List<Long> mainUserList = Arrays.asList(1L, 2L);

    /**
     * 定时缓存用户推荐信息。
     * 该方法使用CRON表达式每天凌晨3点执行一次，用于批量缓存主要用户的推荐信息，以减少数据库查询压力。
     * 使用Redis作为缓存存储，缓存有效期为30秒。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void doCacheRecommendUse() {
        // 尝试获取分布式锁，确保并发安全
        RLock lock = redissonClient.getLock("teamsmatcher:precache:docache:lock");
        try {
            // 尝试锁定，如果在30秒内获得锁，则继续执行
            if (lock.tryLock(0L, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock" + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    // 查询用户，此处示例查询条件为空，实际应用中可根据需求添加
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("teamsmatcher:user:recommend:%s", userId);
                    // 获取Redis的值操作接口
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        // 将查询到的推荐用户列表缓存在Redis中，设置过期时间为30秒
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            // 释放锁，确保锁的正确释放，避免死锁
            if(lock.isHeldByCurrentThread()) {
                System.out.println("unLock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}