package tech.jxing.teams_matcher.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import tech.jxing.teams_matcher.model.domain.Team;
import tech.jxing.teams_matcher.service.TeamService;
import tech.jxing.teams_matcher.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author JunXing
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-04-09 10:15:44
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




