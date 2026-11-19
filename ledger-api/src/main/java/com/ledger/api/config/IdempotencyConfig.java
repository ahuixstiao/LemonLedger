package com.ledger.api.config;

import com.ledger.api.idempotent.IdempotencyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册提交幂等拦截器：所有后台与前台写接口都生效，没带 X-Idempotency-Key 的请求照常放行。
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class IdempotencyConfig implements WebMvcConfigurer {

    private final IdempotencyInterceptor idempotencyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/admin/**", "/emp/**")
                .excludePathPatterns("/admin/factoryPieceRate/imageView/**");
    }
}
