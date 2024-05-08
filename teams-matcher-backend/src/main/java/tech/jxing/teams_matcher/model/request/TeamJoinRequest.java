package tech.jxing.teams_matcher.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 加入队伍请求包装类
 *
 * @author JunXing
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -4110988328259358783L;

    /**
     * 队伍Id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String password;
}
