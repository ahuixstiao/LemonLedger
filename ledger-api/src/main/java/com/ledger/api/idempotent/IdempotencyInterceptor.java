package com.ledger.api.idempotent;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 读请求头 X-Idempotency-Key，对写操作做一次去重：
 * 同一个 key 在 10 分钟内只放行一次，重复的直接回一句提示，不再调用业务。
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class IdempotencyInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Idempotency-Key";
    private static final String ATTR = "idempotencyKey";

    private final IdempotencyGuard guard;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return true;
        }
        String key = request.getHeader(HEADER);
        if (key == null || key.isBlank()) {
            return true;
        }
        if (!guard.tryAcquire(key)) {
            writeDuplicate(response);
            return false;
        }
        request.setAttribute(ATTR, key);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object key = request.getAttribute(ATTR);
        if (key == null) {
            return;
        }
        // 只有成功（2xx）才继续占用这个 key；失败要释放，否则用户重试会被当成重复提交
        if (ex != null || response.getStatus() < 200 || response.getStatus() >= 300) {
            guard.release((String) key);
        }
    }

    private void writeDuplicate(HttpServletResponse response) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 409);
        body.put("message", "这次提交已经处理过了，不用再点一次");
        body.put("data", null);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}
