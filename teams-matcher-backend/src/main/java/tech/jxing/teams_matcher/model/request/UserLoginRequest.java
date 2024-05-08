package tech.jxing.teams_matcher.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 * @author JunXing
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -2089303960881003744L;

    private String userAccount;

    private String userPassword;
}
