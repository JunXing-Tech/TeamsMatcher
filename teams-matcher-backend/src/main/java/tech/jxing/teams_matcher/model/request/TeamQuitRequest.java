package tech.jxing.teams_matcher.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 *
 * @author JunXing
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -2046672212606282110L;

    /**
     * 队伍Id
     */
    private Long teamId;
}
