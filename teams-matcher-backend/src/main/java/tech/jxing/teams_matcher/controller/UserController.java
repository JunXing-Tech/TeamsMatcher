package tech.jxing.teams_matcher.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import tech.jxing.teams_matcher.common.BaseResponse;
import tech.jxing.teams_matcher.common.ErrorCode;
import tech.jxing.teams_matcher.common.ResultUtils;
import tech.jxing.teams_matcher.model.domain.User;
import tech.jxing.teams_matcher.model.request.UserLoginRequest;
import tech.jxing.teams_matcher.model.request.UserRegisterRequest;
import tech.jxing.teams_matcher.exception.BusinessException;
import tech.jxing.teams_matcher.model.vo.UserVO;
import tech.jxing.teams_matcher.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static tech.jxing.teams_matcher.constant.UserConstant.USER_LOGIN_STATE;


/**
 * @author JunXing
 * @RestController 表示该类是一个控制器类，所有的处理方法的返回结果都直接写入HTTP响应体中，而不是跳转到视图页面
 * @RequestMapping 表示该控制器处理的URL路径的公共部分，即所有的请求路径都以"/user"开头
 * @CrossOrigin(origins={"http://localhost:5173"}) 表示允许跨域访问，允许的请求来源是"http://localhost:5173"
 * @RequestBody 注解将请求体中的数据绑定到User对象上，可以直接在方法中获取到请求体中的数据，并进行相应的处理
 * 用户接口 - 控制层封装请求
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"})
@Slf4j
public class UserController {

    @Resource // 表示注入一个由名称指定的资源，在这里是注入了UserService
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register") // 表示将HTTP POST请求映射到处理方法上，处理的路径是"/user/register"。
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        //对请求体进行了空值和空字符串的校验
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        // controller 层倾向于对 *请求参数本身的校验*，不涉及业务逻辑本身
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            return null;
        }

        long result  = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 处理用户登录请求。
     *
     * @param userLoginRequest 包含用户登录信息的请求体，如用户名和密码。
     * @param request 用户的HTTP请求，可用于获取额外的请求信息。
     * @return 如果登录成功，返回一个包含用户信息的响应；如果登录失败或请求无效，返回null。
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 验证登录请求对象是否为空
        if (userLoginRequest == null) {
            return null;
        }
        // 提取用户名和密码
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 检查用户名和密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        // 执行用户登录逻辑
        User user = userService.userLogin(userAccount, userPassword, request);
        // 返回登录结果
        return ResultUtils.success(user);
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            return null;
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /** 获取用户当前状态 */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        // 获得用户的登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 转换成User对象
        User currentUser = (User) userObj;
        // 转换成User对象
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // 通过用户Id来查询数据库中的用户数据
        User user = userService.getById(userId);
        // 返回脱敏后的用户数据
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 查询全部用户（仅管理员可用）
     * @param username 查询条件，用户名（可选）
     * @param request HttpServletRequest对象，用于判断请求用户是否为管理员
     * @return 返回用户列表的基础响应对象，如果用户不是管理员，则返回错误信息
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){
        // 判断请求用户是否为管理员
        if(!userService.isAdmin(request)){
            // 如果不是管理员，抛出业务异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            // 如果提供了用户名，则添加模糊查询条件
            queryWrapper.like("username", username);
        }
        // 根据查询条件查询用户列表
        List<User> userList = userService.list(queryWrapper);
        // 对查询结果进行处理，获取安全的用户信息列表
        List<User> list = userList.stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
        // 返回处理后的用户列表
        return ResultUtils.success(list);
    }


    /**
     * 根据标签名列表搜索用户。
     *
     * @param tagNameList 标签名列表，不能为空。
     * @return 返回用户列表的成功响应。
     * @throws BusinessException 如果标签名列表为空，抛出此异常。
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        // 检查标签名列表是否为空，为空则抛出业务异常
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 调用服务层方法，根据标签名列表搜索用户
        List<User> userList = userService.searchUserByTags(tagNameList);
        // 构造并返回搜索用户成功的响应
        return ResultUtils.success(userList);
    }

    /**
     * 获取推荐用户列表
     *
     * @param pageSize 每页显示的用户数量
     * @param pageNum 当前页码
     * @param request HTTP请求对象，可用于传递额外的查询条件（本示例未使用）
     * @return 返回推荐用户列表的响应信息，包括用户分页数据。
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 获取登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 构造Redis中存储推荐用户列表的键名
        String redisKey = String.format("teamsmatcher:user:recommend:%s", loginUser.getId());
        // 从Redis获取推荐用户列表
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        // 如果Redis中存在推荐用户列表，则直接返回
        if(userPage != null) {
            return ResultUtils.success(userPage);
        }

        // 如果redis中没有缓存，则从数据库中查询推荐用户列表
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            // 将查询到的推荐用户列表存储到Redis中
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 记录Redis存储异常日志
            log.error("redis set key error", e);
        }
        // 返回查询到的推荐用户列表
        return ResultUtils.success(userPage);
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @param request HttpServletRequest对象，用于获取登录用户的信息
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        if(user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务层方法更新用户信息
        Integer result = userService.updateUser(user, loginUser);
        // 返回更新成功结果
        return ResultUtils.success(result);
    }


    /** 用户逻辑删除（仅管理员可用） */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        // 仅管理员可删除
        if(! userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的TOP N 用户
     * @param num 指定匹配的用户数量
     * @param request 用户的请求对象，用于获取当前登录的用户信息
     * @return 返回最匹配的用户列表
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if(num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

}
