package tech.jxing.teams_matcher.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 * @author JunXing
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 5679023018454962004L;

    private String userAccount;

    private String userPassword;
}
