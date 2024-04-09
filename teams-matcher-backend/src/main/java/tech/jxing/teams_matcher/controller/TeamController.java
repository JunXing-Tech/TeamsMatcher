package tech.jxing.teams_matcher.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import tech.jxing.teams_matcher.common.BaseResponse;
import tech.jxing.teams_matcher.common.ErrorCode;
import tech.jxing.teams_matcher.common.ResultUtils;
import tech.jxing.teams_matcher.exception.BusinessException;
import tech.jxing.teams_matcher.model.domain.Team;
import tech.jxing.teams_matcher.model.dto.TeamQuery;
import tech.jxing.teams_matcher.service.TeamService;
import tech.jxing.teams_matcher.service.UserService;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 添加队伍信息
     * @param team 队伍对象，包含队伍的详细信息
     * @return 返回操作结果，包含新增队伍的ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team) {
        // 验证参数是否为空
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 保存队伍信息到数据库
        boolean save = teamService.save(team);
        // 数据保存结果校验
        if(!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        // 返回成功响应，包含队伍ID
        return ResultUtils.success(team.getId());
    }


    /**
     * 根据ID删除队伍信息
     * @param id 要删除的队伍ID
     * @return 返回操作结果，表示删除是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id){
        // 验证ID是否合法
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误Id");
        }
        // 根据ID从数据库删除队伍信息
        boolean result = teamService.removeById(id);
        // 删除操作结果校验
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        // 返回成功响应，表示删除成功
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍信息
     * @param team 包含队伍更新信息的对象，不能为null
     * @return 返回一个包含更新成功与否的布尔值的响应对象
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 通过ID更新队伍信息
        boolean result = teamService.updateById(team);
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
     * 获取队伍列表
     *
     * @param teamQuery 队伍查询条件，用于指定查询参数
     * @return 返回队伍列表的响应信息，包含查询到的队伍列表
     */
    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery) {
        if(teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Team team = new Team();
        // 将查询条件对象的属性值复制到Team对象中，用于构建查询Wrapper
        BeanUtils.copyProperties(teamQuery, team);
        // 创建查询包装器，用于指定查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        // 返回一个List<Team>，即查询结果中所有满足条件的Team对象集合
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询团队列表
     *
     * @param teamQuery 包含分页信息和团队查询条件的对象
     * @return 返回团队信息的分页响应对象
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
}