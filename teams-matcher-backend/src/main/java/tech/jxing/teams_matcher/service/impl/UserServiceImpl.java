package tech.jxing.teams_matcher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tech.jxing.teams_matcher.common.ErrorCode;
import tech.jxing.teams_matcher.model.domain.User;
import tech.jxing.teams_matcher.exception.BusinessException;
import tech.jxing.teams_matcher.service.UserService;
import tech.jxing.teams_matcher.mapper.UserMapper;
import org.springframework.stereotype.Service;
import tech.jxing.teams_matcher.utils.AlgorithmUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static tech.jxing.teams_matcher.constant.UserConstant.ADMIN_ROLE;
import static tech.jxing.teams_matcher.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @Resource 注解可以指定要注入的组件或资源的名称或类型，更加灵活地控制注入行为
 * @author JunXing
 * @description 针对表【user.sql(用户)】的数据库操作Service实现
 * @createDate 2024-04-09 10:31:08
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 定义盐值
     */
    final static String SALT = "junxing";

    /**
     StringUtils.isAnyBlank是Apache Commons Lang库中的一个方法
     用于检查给定的字符串数组中是否有任何一个字符串为null、空字符串或者只包含空格
     如果数组中有任何一个字符串满足上述条件，则返回true，否则返回false。
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 校验用户的账户、密码、校验密码，是否符合要求
        // 非空
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于 4 位
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度小于4位");
        }
        // 密码不小于 8 位
        if(userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码小于8位");
        }
        // 账户不包含特殊字符
        String validPattern = "/^[a-zA-Z0-9]{8,}$/";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return -1;
        }
        // 密码和校验密码必须相同
        if(!userPassword.equals(checkPassword)){
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        long count = userMapper.selectCount(queryWrapper.eq("userAccount", userAccount));
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        // 非空判断
        if(!saveResult){
            return -1;
        }
        // 返回用户 id
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            return  null;
        }
        if(userAccount.length() < 4){
            return null;
        }
        if(userPassword.length() < 8){
            return null;
        }
        String validPattern = "/^[a-zA-Z0-9]{8,}$/";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return null;
        }
        // 加密后与数据库的加密密码进行比对
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        // userMapper.selectOne()通常是用于从数据库中查询一个对象的方法
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null){
            // 日志报错提醒
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        // 返回脱敏后的用户信息
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser 原始用户信息
     * @return 用户脱敏信息
     */
    @Override
    public User getSafetyUser(User originUser){
        if(originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request 获取用户登录态（Session），并移除以实现用户注销的功能
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户 [内存查询]
     *
     * @param tagNameList 用户拥有的标签
     * @return 符合标签的用户
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.全量查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        //2. 反序列化：把 json 转为 java 对象
        Gson gson = new Gson();
        //3. 在内存中判断是否包含是否含有指定的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName : tagNameList) {
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息。
     *
     * @param user 需要更新的用户对象。
     * @param loginUser 当前登录的用户对象。
     * @return 更新成功返回更新行数，失败抛出异常。
     * @throws BusinessException 如果用户ID不合法、用户无权限进行更新操作或用户不存在时抛出异常。
     */
    @Override
    public int updateUser(User user, User loginUser) {
        // 检查用户是否为空
        if(user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long userId = user.getId();
        // 检查用户ID是否合法
        if(userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员则允许更新任意用户，反之则只允许更新自己的信息
        if(!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 根据用户ID更新用户信息
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否为空
        if(request == null) {
            return null;
        }
        // 获取用户登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 判断用户是否有登录态
        if(userObj == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 判断用户是否为管理员
     * @param request 从客户端传来的HTTP请求
     * @return true - 当前用户是管理员；false - 当前用户是普通用户或未登录
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 从请求的会话中获取登录状态的用户对象
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        // 如果用户对象为空或用户角色不是管理员，则返回false
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否为管理员
     * @param loginUser 登录用户信息
     * @return true - 当前用户是管理员；false - 当前用户是普通用户或未登录
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 根据登录用户的标签，返回匹配度最高的前num个用户
     *
     * @param num 需要返回的匹配用户数量
     * @param loginUser 当前登录用户，用于提取标签进行匹配
     * @return 匹配度最高的前num个用户
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        // 设置查询条件，只查询标签不为空的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        // 获取所有用户列表
        List<User> userList = this.list(queryWrapper);
        // 获取登录用户的标签，并转换为列表
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagsList = gson.fromJson(tags, new TypeToken<List<String>>(){}.getType());

        List<Pair<User, Long>> list = new ArrayList<>();

        // 计算所有用户与登录用户的标签匹配度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 跳过没有标签的用户和当前登录用户
            if(StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            // 将用户标签转换为列表进行匹配度计算
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());
            // 计算当前用户与登录用户的标签匹配度
            long distance = AlgorithmUtils.miniDistance(tagsList, userTagsList);
            // 保存匹配用户的用户信息和匹配度到list中
            list.add(new Pair<>(user, distance));
        }
        // 根据匹配度排序，选择前num个匹配度最高的用户
        List<Pair<User, Long>> topUserPairList = list.stream()
                /**
                 * 若差值为正数，表示a.getValue()大于b.getValue()，此时a应排在b后面；
                 * 若差值为负数，表示a.getValue()小于b.getValue()，此时a应排在b前面；
                 */
                .sorted((a, b) -> (int)(a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        // 获取最终需要返回的用户ID列表
        List<Long> userIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        // 根据用户ID列表查询用户信息，确保用户数据完整
        QueryWrapper<User> userQueryWrapper = new QueryWrapper();
        userQueryWrapper.in("id", userIdList);
        /**
         * 根据用户查询条件，生成一个映射表，其键为用户ID，值为对应ID的用户列表。
         * 这个方法首先根据用户查询条件查询用户列表，然后对每个用户应用getSafetyUser方法进行处理，
         * 最后将处理后的用户按其ID进行分组，生成一个映射表
         */
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        /**
         * 根据用户ID列表，从映射中检索用户并构建一个最终用户列表。
         *
         * @param userIdList 用户ID的列表，这些ID用于从映射中检索用户。
         * @param userIdUserListMap 一个映射，其中键是用户ID，值是与该ID相关联的用户列表。
         * @return finalUserList 包含从映射中检索到的用户的列表。每个用户是根据给定的用户ID列表中的ID从映射中获取的第一个用户。
         */
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }
}




