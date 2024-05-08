package tech.jxing.teams_matcher.common;

/**
 * 定义错误码
 * @author JunXing
 */
public enum ErrorCode {

    // 成功操作
    SUCCESS(0, "ok", ""),
    // 请求参数错误
    PARAMS_ERROR(40000, "请求参数错误", ""),
    // 请求参数为空
    NULL_ERROR(40001, "请求数据为空", ""),
    // 未登录
    NOT_LOGIN(40100, "未登录", ""),
    // 无权限
    NO_AUTH(40101, "无权限", ""),
    // 禁止操作
    FORBIDDEN(40301, "禁止操作", ""),
    // 系统内部异常
    SYSTEM_ERROR(50000, "系统内部异常", "");

    /** 错误码 */
    private final int code;
    /** 错误码信息 */
    private final String message;
    /** 错误码描述 */
    private final String description;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    /** 枚举值不支持set方法，支持get方法*/
    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
