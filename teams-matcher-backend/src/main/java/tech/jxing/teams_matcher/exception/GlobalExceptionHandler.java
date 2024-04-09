package tech.jxing.teams_matcher.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.jxing.teams_matcher.common.BaseResponse;
import tech.jxing.teams_matcher.common.ErrorCode;
import tech.jxing.teams_matcher.common.ResultUtils;

/**
 * @RestControllerAdvice 是一个注解，用于定义全局异常处理程序和全局数据绑定
 * 可以与@RestController、@Controller和@ResponseBody一起使用，以捕获在请求期间抛出的异常并将它们转换为HTTP响应
 * 在一个应用程序中，只需要声明一个@RestControllerAdvice类即可处理所有控制器的异常
 * 可以通过编写带有@ExceptionHandler注解的方法来处理特定的异常，并将它们映射到HTTP响应
 * 全局异常处理器
 * @author JunXing
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常的全局异常处理器。
     * 当抛出 BusinessException 异常时，此处理器将捕获异常并返回相应的错误响应。
     *
     * @param e BusinessException 异常实例，包含错误代码、消息和描述。
     * @return BaseResponse 错误响应对象，包含错误代码、消息和可能的描述。
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        // 记录业务异常日志
        log.error("businessException" + e.getMessage(), e);
        // 构造并返回业务异常响应
        return ResultUtils.
                error(e.getCode(), e.getMessage(), e.getDescription());
    }

    /**
     * 处理运行时异常的全局异常处理器。
     * 当抛出 RuntimeException 异常或其子类时，此处理器将捕获异常并返回通用的系统错误响应。
     *
     * @param e RuntimeException 异常实例，包含错误消息。
     * @return BaseResponse 错误响应对象，包含系统错误代码、异常消息和空描述。
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        // 记录运行时异常日志
        log.error("runtimeException", e);
        // 构造并返回运行时异常响应
        return ResultUtils.
                error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
