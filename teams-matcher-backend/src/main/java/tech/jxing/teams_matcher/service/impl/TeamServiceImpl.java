package tech.jxing.teams_matcher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import tech.jxing.teams_matcher.common.ErrorCode;
import tech.jxing.teams_matcher.exception.BusinessException;
import tech.jxing.teams_matcher.model.domain.Team;
import tech.jxing.teams_matcher.model.domain.User;
import tech.jxing.teams_matcher.model.domain.UserTeam;
import tech.jxing.teams_matcher.model.dto.TeamQuery;
import tech.jxing.teams_matcher.model.enums.TeamStatusEnum;
import tech.jxing.teams_matcher.model.request.TeamJoinRequest;
import tech.jxing.teams_matcher.model.request.TeamQuitRequest;
import tech.jxing.teams_matcher.model.request.TeamUpdateRequest;
import tech.jxing.teams_matcher.model.vo.TeamUserVO;
import tech.jxing.teams_matcher.model.vo.UserVO;
import tech.jxing.teams_matcher.service.TeamService;
import tech.jxing.teams_matcher.mapper.TeamMapper;
import org.springframework.stereotype.Service;
import tech.jxing.teams_matcher.service.UserService;
import tech.jxing.teams_matcher.service.UserTeamService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author JunXing
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-04-09 10:15:44
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private TeamMapper teamService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 添加队伍及其创建者到数据库。
     * @param team 队伍信息对象，包含队伍的名称、描述、状态等。
     * @param loginUser 登录用户对象，标识当前创建队伍的用户。
     * @return 返回新创建的队伍ID。
     * @throws BusinessException 如果创建过程中出现任何业务规则错误，抛出此异常。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(Team team, User loginUser) {

        // 队伍最大人数
        final long maxNumberOfTeamMembers = 20;
        // 队伍简介最大长度
        final long teamIntroductionMaxValue = 512;
        // 队伍密码最大长度
        final long maxLengthOfTeamPassword = 32;
        // 单个用户队伍创建上限
        final long maxNumberOfTeamsCreatedByUser = 5;

        // 1.请求参数是否为空
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.用户是否登录，未登录不允许创建队伍
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 3.校验队伍信息是否合法
        // 3.1 队伍人数 > 1 && <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum > maxNumberOfTeamMembers) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        // 3.2 队伍名称长度 <= 20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() > maxNumberOfTeamMembers) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        // 3.3 队伍描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > teamIntroductionMaxValue) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        // 3.4 队伍状态是否公开，默认公开（0 - 公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // 3.5 如状态为非公开状态，则一定要有密码，且密码长度 <= 32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET_STATUS.equals(teamStatusEnum)) {
            if(StringUtils.isBlank(password) || password.length() > maxLengthOfTeamPassword) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过长");
            }
        }
        // 3.6 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间不满足要求");
        }
        // 3.7 校验用户最多创建 5 个队伍
        // TODO 有BUG 用户可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        final long userId = loginUser.getId();
        // 设置查询条件，查询与当前用户ID匹配的队伍
        queryWrapper.eq("userId", userId);
        // 计算当前用户创建的队伍数量
        long hasTeamNum = this.count(queryWrapper);
        // 如果已创建的队伍数量超过了最大允许值，则抛出业务异常
        if(hasTeamNum >= maxNumberOfTeamsCreatedByUser) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建 5 个队伍");
        }
        // 4. 插入队伍信息到队伍表
        // 设置队伍ID为空，以便生成新的ID
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        // 获取保存后的队伍ID
        Long teamId = team.getId();
        if(!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 5. 插入用户到队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 根据条件查询队伍列表。
     *
     * @param teamQuery 包含查询条件的对象，可以指定队伍的ID、名称、描述、最大成员数、创建者ID、状态等。
     * @param isAdmin 是否为管理员，管理员可见所有队伍，非管理员只能查看公开队伍。
     * @return 返回满足查询条件的队伍列表，每个队伍包含基本信息和创建者信息。
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 根据传入查询对象生成单个或多个的组合查询条件
        if (teamQuery != null) {
            // 处理队伍ID查询条件
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }

            /**
             * 处理队伍ID列表查询条件
             */
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }

            // 处理队伍名称查询条件
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 处理队伍描述查询条件
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            // 处理通过关键字搜索队伍的条件
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)) {
                // 模糊匹配队伍名称或描述
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 处理最大成员数查询条件
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 处理队伍创建者ID查询条件
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 获取当前队伍状态（公开 / 加密 / 私有）
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            // 如传入的状态值为空，则默认为公开
            if(statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC_STATUS;
            }
            // 只有管理员才能查看私有队伍
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE_STATUS)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限查看私有");
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // 不展示已过期队伍
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        // 如果查询队伍列表为空，则返回一个空列表
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 关联查询队伍列表中的每个队伍的创建者信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            // 忽略userId为空的队伍
            if(userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 用户信息脱敏
            if(user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest 包含更新请求的对象，不能为null
     * @param loginUser 当前登录的用户，用于权限验证，不能为null
     * @return 更新成功返回true，失败返回false
     * @throws BusinessException 当请求参数为空、队伍Id为空、队伍不存在、用户无权限修改、加密队伍未设置密码时，抛出业务异常
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍Id为空");
        }
        // 根据ID查询旧的队伍信息
        Team oldTeam = this.getById(id);
        if(oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 验证用户是否有权限修改队伍
        if(!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限修改该队伍");
        }
        // 校验队伍状态，如果是加密队伍，检查密码是否设置
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET_STATUS)) {
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码");
            }
        }
        // 复制更新请求到新的队伍对象
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        // 更新队伍信息
        return this.updateById(updateTeam);
    }

    // TODO 在短时间内多次加入队伍，可能导致重复加入队伍问题，可用锁来解决
    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest 加入队伍请求对象，包含队伍ID和密码（如果队伍为加密队伍）
     * @param loginUser 登录用户对象，用于标识请求的用户
     * @return 成功加入队伍返回true，否则返回false
     * @throws BusinessException 当请求参数错误、队伍不存在、队伍已过期、不可加入私有队伍、密码错误、已达到加入队伍的最大限制、已加入该队伍或队伍已满时抛出
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        // 验证请求参数是否为空
        if(teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        // 校验队伍ID的有效性
        Long teamId = teamJoinRequest.getTeamId();
        // 根据ID查询队伍信息
        Team team = getTeamById(teamId);
        // 检查队伍是否过期，只能加入未过期的队伍
        if(team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 不可加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum.equals(TeamStatusEnum.PRIVATE_STATUS)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不可加入私有队伍");
        }
        // 如队伍为加密状态，则必须密码匹配
        String password = teamJoinRequest.getPassword();
        if(teamStatusEnum.SECRET_STATUS.equals(teamStatusEnum)) {
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        /**
         * 分布式锁解决短时间频繁加入队伍，导致重复加入
         */
        // 检查用户已加入的队伍数量是否达到上限，最多加入5个队伍
        Long userId = loginUser.getId();
        RLock lock = redissonClient.getLock("teamsmatcher:join_team");
        try {
            // 尝试获取锁
            while(true) {
                if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("get lock" + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWraper = new QueryWrapper<>();
                    userTeamQueryWraper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWraper);
                    if(hasJoinNum >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
                    }
                    // 检查用户是否已加入该队伍
                    userTeamQueryWraper = new QueryWrapper<>();
                    userTeamQueryWraper.eq("userId", userId);
                    userTeamQueryWraper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWraper);
                    if(hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 检查队伍是否已满员
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if(teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 保存用户加入队伍的信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("get lock failed", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if(lock.isHeldByCurrentThread()) {
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
    /**
     * 用户退出队伍接口
     *
     * @param teamQuitRequest 包含队伍退出请求信息的对象，其中需要有队伍ID
     * @param loginUser 当前登录的用户信息，用于判断退出队伍的用户
     * @return 如果退出成功，返回true；否则抛出异常
     * @throws BusinessException 当参数错误、队伍不存在、用户未加入队伍或系统错误时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 校验请求参数是否为空
        if(teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long teamId = teamQuitRequest.getTeamId();
        // 根据ID查询队伍信息
        Team team = getTeamById(teamId);
        Long userId = loginUser.getId();
        // 判断用户是否加入了队伍
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if(count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        // 计算队伍中还剩余的成员数量
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍还剩一人，直接解散队伍
        if(teamHasJoinNum == 1) {
            this.removeById(teamId);
        } else {
            // 如果队伍中还有多人，判断退出的用户是否为队长
            if(team.getUserId() == userId) {
                // 队长退出队伍，需要转移队长权限给最早加入的成员
                // 查询最早加入的两个成员
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                // 确保有至少两个成员
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                // 转移队长给第二早加入的成员
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserId = nextUserTeam.getUserId();
                Team updatedTeam = new Team();
                updatedTeam.setId(teamId);
                updatedTeam.setUserId(nextUserId);
                boolean result = this.updateById(updatedTeam);
                // 更新队长信息失败，抛出异常
                if(!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 删除该用户和队伍的关系
        return userTeamService.remove(queryWrapper);
    }


    /**
     * 删除队伍
     * @param id 队伍ID
     * @param loginUser 登录用户
     * @return 删除成功返回true，失败返回false
     * @Transactional 注解指明该方法为事务方法，任何异常都将回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = this.getTeamById(id);
        Long teamId = team.getId();
        // 校验用户是否为队长
        if(!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "禁止解散操作");
        }
        // 移除所有加入队伍的成员
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 校验队伍是否存在
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        // 校验队伍ID是否有效
        if(teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍Id为空");
        }
        // 根据队伍ID查询队伍信息
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取队伍的当前人数
     * @param teamId 队伍Id
     * @return 队伍的当前人数
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWraper = new QueryWrapper<>();
        userTeamQueryWraper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWraper);
    }
}



