package tech.jxing.teams_matcher.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import tech.jxing.teams_matcher.common.BaseResponse;
import tech.jxing.teams_matcher.common.DeleteRequest;
import tech.jxing.teams_matcher.common.ErrorCode;
import tech.jxing.teams_matcher.common.ResultUtils;
import tech.jxing.teams_matcher.exception.BusinessException;
import tech.jxing.teams_matcher.model.domain.Team;
import tech.jxing.teams_matcher.model.domain.User;
import tech.jxing.teams_matcher.model.domain.UserTeam;
import tech.jxing.teams_matcher.model.dto.TeamQuery;
import tech.jxing.teams_matcher.model.request.TeamAddRequest;
import tech.jxing.teams_matcher.model.request.TeamJoinRequest;
import tech.jxing.teams_matcher.model.request.TeamQuitRequest;
import tech.jxing.teams_matcher.model.request.TeamUpdateRequest;
import tech.jxing.teams_matcher.model.vo.TeamUserVO;
import tech.jxing.teams_matcher.service.TeamService;
import tech.jxing.teams_matcher.service.UserService;
import tech.jxing.teams_matcher.service.UserTeamService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author JunXing
 * 队伍接口 - 控制层封装请求
 */

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"})
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * 添加队伍信息
     *
     * @param teamAddRequest 包含队伍添加信息的请求体
     * @param request 用户的请求信息，用于获取登录用户信息
     * @return 添加成功后返回的队伍ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        // 验证参数是否为空
        if(teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 获取登录用户信息
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        // 将请求体中的属性值复制到Team对象中
        BeanUtils.copyProperties(teamAddRequest, team);
        // 调用服务，添加队伍，并获取添加后的队伍Id
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }


    /**
     * 根据ID删除队伍信息
     * @param deleteRequest 要删除的队伍ID
     * @param request 用户的请求信息，用于获取登录用户信息
     * @return 返回操作结果，表示删除是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        // 验证ID是否合法
        if(deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误Id");
        }
        long teamId = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        // 根据ID从数据库删除队伍信息
        boolean result = teamService.deleteTeam(teamId, loginUser);
        // 删除操作结果校验
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        // 返回成功响应，表示删除成功
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍信息
     * @param teamUpdateRequest 包含队伍更新信息的对象，不能为null
     * @return 返回一个包含更新成功与否的布尔值的响应对象
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if(teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 获取当前用户的登录信息
        User loginUser = userService.getLoginUser(request);
        // 通过ID更新队伍信息
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取指定ID的队伍信息
     * @param id 队伍的唯一标识符，必须大于0
     * @return 返回一个包含指定队伍信息的响应对象
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误Id");
        }
        // 通过ID获取队伍信息
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResultUtils.success(team);
    }


    /**
     * 获取列表
     *
     * @param teamQuery 查询队伍的条件对象，不可为null
     * @param request HttpServletRequest对象，用于判断当前用户是否为管理员
     * @return 返回一个包含队伍信息的响应对象，其中队伍信息包括是否已加入该队伍的标志
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 判断当前用户是否为管理员
        boolean isAdmin = userService.isAdmin(request);
        // 根据查询条件和用户是否为管理员获取队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        /**
         * 判断当前用户是否已加入队伍
         */
        List<Long> teamIdList = teamList.stream()
                .map(TeamUserVO::getId)
                .collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try{
            User loginUser = userService.getLoginUser(request);
            // 查询条件为
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍Id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream()
                    .map(UserTeam::getTeamId)
                    .collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}

        /**
         * 查询已加入队伍的人数
         */
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 根据队伍Id，把加入这个队伍的用户列表进行分组
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });

        return ResultUtils.success(teamList);
    }

    // TODO 分页查询队伍列表
    /**
     * 分页查询队伍列表
     *
     * @param teamQuery 包含分页信息和队伍查询条件的对象
     * @return 返回队伍信息的分页响应对象
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if(teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 创建一个Team实例，并从teamQuery复制属性值
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        // 根据查询参数创建分页对象
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        // 执行分页查询
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * 处理用户加入队伍的请求。
     *
     * @param teamJoinRequest 包含用户加入队伍的相关信息的请求体。
     * @param request 用户的请求对象，用于获取登录用户信息。
     * @return 返回一个基础响应对象，包含操作结果是否成功的布尔值。
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if(teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 处理加入队伍的逻辑，并返回结果
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 处理用户退出团队的请求。
     *
     * @param teamQuitRequest 包含退出团队所需信息的请求体，不能为空。
     * @param request 用户的请求对象，用于获取登录用户信息。
     * @return 返回一个基础响应对象，包含操作结果是否成功的布尔值。
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户创建的队伍列表
     *
     * @param teamQuery 包含队伍查询条件的对象，例如页码、每页数量等
     * @param request 用户的请求对象，用于获取登录用户信息
     * @return 返回一个包含查询结果的响应对象，其中查询结果是用户创建的队伍列表
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取用户加入的队伍列表
     *
     * @param teamQuery 队伍查询条件，用于过滤队伍列表
     * @param request 用户的请求，用于获取登录用户信息
     * @return 返回读完iu列表的响应信息
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        // 构建查询条件，查询当前用户所有的队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        /** 取出不重复的队伍ID */
        // 将用户队伍列表按队伍ID分组，以便后续处理
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        // 提取分组后的队伍ID列表
        List<Long> idList = new ArrayList<>(listMap.keySet());
        // 将队伍ID列表设置到查询条件中
        teamQuery.setIdList(idList);
        // 查询满足条件的队伍列表，包括团队的详细信息
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        // 遍历队伍列表，设置是否已加入字段为true
        teamList.forEach(team -> {
            boolean hasJoin = listMap.get(team.getId()).size() > 0;
            team.setHasJoin(hasJoin);
        });
        return ResultUtils.success(teamList);
    }
}