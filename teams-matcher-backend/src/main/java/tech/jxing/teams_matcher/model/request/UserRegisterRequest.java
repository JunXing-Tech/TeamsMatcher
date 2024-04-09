package tech.jxing.teams_matcher.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author JunXing
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 5679023018454962004L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
