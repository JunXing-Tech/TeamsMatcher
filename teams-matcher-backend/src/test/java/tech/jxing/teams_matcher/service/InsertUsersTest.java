package tech.jxing.teams_matcher.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;
import tech.jxing.teams_matcher.model.domain.User;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 用户服务测试类，用于测试用户插入功能的性能
 * @author JunXing
 */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    // 创建一个线程池，用于并发测试
    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 测试单线程批量插入用户数据。
     */
    @Test
    public void insertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("TestUserName");
            user.setUserAccount("TestUserAccount_" + i);
            user.setAvatarUrl("https://gd-hbimg.huaban.com/81d7a3418e79cd6a8fa4a578876c22dba2b6c43a633e-AssrC3_fw1200webp");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("12345678910");
            user.setEmail("TestEmail@Email.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 测试并发批量插入用户数据。
     */
    @Test
    public void concurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 分十组进行并发插入
        int batchSize = 10000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            // 循环创建用户数据，每batchSize个分组
            while(true) {
                j++;
                User user = new User();
                user.setUsername("TestUserName");
                user.setUserAccount("TestUserAccount_" + j);
                user.setAvatarUrl("https://gd-hbimg.huaban.com/81d7a3418e79cd6a8fa4a578876c22dba2b6c43a633e-AssrC3_fw1200webp");
                user.setGender(0);
                user.setUserPassword("123456789");
                user.setPhone("12345678910");
                user.setEmail("TestEmail@Email.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if( j  % batchSize == 0) {
                    break;
                }
            }
            // 异步执行插入任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        // 等待所有异步任务完成
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
