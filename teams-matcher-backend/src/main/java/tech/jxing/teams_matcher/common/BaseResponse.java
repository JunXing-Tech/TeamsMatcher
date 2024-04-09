package tech.jxing.teams_matcher.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @author JunXing
 * @param <T> 泛型便于接收各种类型的data数据
 */
@Data
public class BaseResponse<T> implements Serializable {

    /** 状态码 */
    private int code;

    /** 返回数据 */
    private T data;

    /** 消息 */
    private String message;

    /** 描述 */
    private String description;

    /** 通过构造函数来创建BaseResponse对象的不同方式，以适应不同的业务需求 */
    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message){
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data){
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

}
