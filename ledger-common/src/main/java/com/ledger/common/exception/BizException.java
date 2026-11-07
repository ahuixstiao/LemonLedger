package com.ledger.common.exception;

import java.io.Serial;

/**
 * 业务异常。
 *
 * <p>用来表示「用户操作不合法」这类可以预期的失败（比如计件单价不存在、账单重复）。
 * 全局异常处理器会把它转换成 {@code Result.fail(消息)}，消息原样返回给前端；
 * 其它未预期的异常则统一返回兜底提示，避免把 SQL、堆栈之类的内部信息暴露出去。</p>
 *
 * <p>约定：Service 层遇到业务规则不满足时抛出本异常，而不是 {@code return Result.fail(...)}
 * 之后继续往下跑，也不是 {@code catch (RuntimeException)} 吞掉——吞掉会让 @Transactional 收不到异常，
 * 该回滚的事务不会回滚。</p>
 */
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
