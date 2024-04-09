package tech.jxing.teams_matcher.service;

import tech.jxing.teams_matcher.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author JunXing
* @description 针对表【user.sql(用户)】的数据库操作Service
* @createDate 2024-04-09 10:31:08
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      session状态
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser 原始用户信息
     * @return 脱敏后的用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户登出
     * @param request session状态
     * @return 0-成功，1-失败
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return 搜索到的用户列表
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @param loginUser 当前登录用户
     * @return 0-成功，1-失败
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户
     * @param request session状态
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request session状态
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser 当前登录用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);
}
