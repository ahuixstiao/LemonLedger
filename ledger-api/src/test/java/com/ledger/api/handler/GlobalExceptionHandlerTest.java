package com.ledger.api.handler;

import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 全局异常处理器的单元测试。
 *
 * <p>不启动 Spring（不连数据库），只验证"异常进 → 统一的 Result 出"这条翻译链。
 * 这条链断了的表现是：前端本来应该弹「该成衣厂不存在」，结果只弹一句「请求失败」。</p>
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("业务异常：消息原样带给前端，状态码是 500")
    void bizException_keepsMessage() {
        Result<Object> result = handler.handleBizException(new BizException("该成衣厂不存在"));

        assertNotNull(result);
        assertEquals(500, result.getStatus());
        assertEquals("该成衣厂不存在", result.getMessage());
    }

    @Test
    @DisplayName("类型不匹配：提示里带上参数名，而不是一串英文异常")
    void typeMismatch_mentionsParameterName() {
        // new MethodArgumentTypeMismatchException 参数比较多，这里直接构造一个最小的
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception =
                new org.springframework.web.method.annotation.MethodArgumentTypeMismatchException(
                        "abc", Integer.class, "factoryId", null, new NumberFormatException("For input string: \"abc\""));

        Result<Object> result = handler.handleTypeMismatch(exception);

        assertEquals(500, result.getStatus());
        assertEquals("参数 factoryId 的格式不正确", result.getMessage());
    }

    @Test
    @DisplayName("兜底异常：不把堆栈信息泄露给前端")
    void unexpectedException_doesNotLeakDetails() {
        Result<Object> result = handler.handleException(new IllegalStateException("数据库连接池已耗尽"));

        assertEquals(500, result.getStatus());
        assertEquals(false, result.getMessage().contains("数据库连接池已耗尽"));
        assertNotNull(result.getMessage());
    }
}
