package com.ledger.api.handler;

import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理。
 *
 * <p>没有它的时候，任何没有被业务代码 try/catch 的异常都会被 Spring 转成一份
 * 固定格式的错误 JSON（{@code status=500, error="Internal Server Error", path=...}），
 * 与项目统一的 {@code {status, message, data}} 结构对不上，
 * 前端拿不到 message，只能显示「请求失败」，排查时也看不到真正的原因。</p>
 *
 * <p>约定：本类只负责「翻译」，不吞异常信息。业务可预期的失败返回 500 + 具体消息
 * （与原有 Result.fail 的语义保持一致，前端据此弹 ElMessage），未预期异常打完整堆栈到日志，
 * 前端只拿到一句兜底提示。</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常：消息可以直接给用户看。
     */
    @ExceptionHandler(BizException.class)
    public Result<Object> handleBizException(BizException exception) {
        log.warn("业务处理失败: {}", exception.getMessage());
        return Result.fail(exception.getMessage());
    }

    /**
     * 请求体校验失败（@Valid + @RequestBody）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数不合法" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return Result.fail(message);
    }

    /**
     * 表单/查询参数绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数不合法" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return Result.fail(message);
    }

    /**
     * 方法参数校验失败（@Validated + @RequestParam/@PathVariable）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("请求参数不合法");
        return Result.fail(message);
    }

    /**
     * 必填的查询参数没传。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Object> handleMissingParameter(MissingServletRequestParameterException exception) {
        return Result.fail("缺少必需的参数: " + exception.getParameterName());
    }

    /**
     * 参数类型不对，例如 ?factoryId=abc。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Object> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return Result.fail("参数 " + exception.getName() + " 的格式不正确");
    }

    /**
     * 请求体不是合法 JSON，或字段类型对不上。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        log.warn("请求体解析失败: {}", exception.getMessage());
        return Result.fail("请求内容格式不正确，请检查提交的数据");
    }

    /**
     * 唯一索引冲突：目前只出现在「员工姓名 / 成衣厂名称」重复时。
     * 把数据库的英文报错翻译成人话，否则前端只能显示 IllegalArgumentException。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Object> handleDuplicateKey(DuplicateKeyException exception) {
        log.warn("唯一约束冲突: {}", exception.getMessage());
        return Result.fail("已存在同名记录（员工姓名或成衣厂名称不能重复），请修改名称后重试");
    }

    /**
     * 其它数据库约束问题：字段超长、非空字段传空、外键不满足等。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Object> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.error("数据不符合数据库约束", exception);
        return Result.fail("数据不符合数据库约束（可能是名称过长或必填项为空），请检查后重试");
    }

    /**
     * 兜底：未被识别的一切异常。
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception exception) {
        log.error("接口处理异常", exception);
        return Result.fail("服务器处理失败，请稍后重试（详细原因见服务端日志）");
    }
}
