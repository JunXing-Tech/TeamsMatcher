package tech.jxing.teams_matcher.service;

import tech.jxing.teams_matcher.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import tech.jxing.teams_matcher.model.domain.User;
import tech.jxing.teams_matcher.model.dto.TeamQuery;
import tech.jxing.teams_matcher.model.request.TeamJoinRequest;
import tech.jxing.teams_matcher.model.request.TeamQuitRequest;
import tech.jxing.teams_matcher.model.request.TeamUpdateRequest;
import tech.jxing.teams_matcher.model.vo.TeamUserVO;

import java.util.List;

/**
* @author JunXing
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-04-09 10:15:44
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team 队伍
     * @param loginUser 创建队伍的用户
     * @return 用户id
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 根据给定的队伍查询条件和用户权限列表获取团队信息。
     *
     * @param teamQuery 包含团队查询条件的对象，用于指定查询的条件
     * @param isAdmin 表示是否为管理员权限，true表示具有管理员权限，false表示普通用户权限。该参数会影响查询结果，比如管理员可能看到更多的队伍信息
     * @return 返回一个队伍用户信息的列表，每个队伍信息包括队伍的基本信息和与之相关联的用户信息。
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest 更新队伍信息的请求对象
     * @param loginUser 当前进行操作的登录用户信息
     * @return boolean 返回true表示信息更新成功，返回false表示更新失败
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍的请求对象
     * @param loginUser 当前进行操作的登录用户信息
     * @return boolean 返回true表示加入成功，返回false表示加入失败
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 处理用户退出团队的请求。
     *
     * @param teamQuitRequest 包含用户退出团队的相关信息，例如队伍ID等。
     * @param loginUser 当前进行操作的登录用户信息。
     * @return 返回一个布尔值，表示用户是否成功退出团队。true表示成功退出，false表示退出失败。
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除与解散队伍
     *
     * @param id 队伍id
     * @param loginUser 当前进行操作的登录用户信息
     * @return boolean 返回true表示删除成功，返回false表示删除失败
     */
    boolean deleteTeam(long id, User loginUser);
}
