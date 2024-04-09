package tech.jxing.teams_matcher.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import tech.jxing.teams_matcher.model.domain.User;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * JunXing
 * 2023/12/17 20:54
 * IntelliJ IDEA
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("junxing");
        user.setUserAccount("123");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setUserPassword("123456789");
        user.setPhone("18973499669");
        user.setEmail("2259143882@qq.com");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        //密码不能为空
        String userAccount = "jxing";
        String userPassword = "";
        String checkPassword = "123456";
        long result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        //用户名 >= 4
        userAccount = "jx";
        result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        //密码不小于8位
        userAccount = "jxing";
        userPassword = "123456";
        result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        //用户名不能有空白字符
        userAccount = "j xing";
        userPassword = "12345678";
        result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        //两次密码不相同
        checkPassword = "123456789";
        result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        //用户名不能重复
        userAccount = "jxing";
        userPassword = "123456789";
        result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        //成功
        userAccount = "jxing";
        result = userService.
                userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertTrue(result > 0);
    }

    // 在测试类中定义一个Mock的HttpServletRequest对象
    @Mock
    HttpServletRequest httpServletRequest;

    @Test
    void userLogin(){
        String userAccount = "jxing";
        String userPassword = "123456789";

        // 使用Mockito为虚拟的HttpServletRequest对象设置行为
        when(httpServletRequest.getParameter("testParam")).thenReturn("testValue");

        User user = userService.userLogin(userAccount, userPassword, httpServletRequest);

        System.out.println(user.getId());
    }

    @Test
    public void testSearchUserByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUserByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}