package com.ledger.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置本地图片目录静态资源映射。
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;

    /**
     * 注册静态资源映射规则。
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path imagePath = Paths.get(imageBaseDir).toAbsolutePath().normalize();
        String resourceLocation = "file:" + imagePath.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/lemonLedgerImages/**")
                .addResourceLocations(resourceLocation);
    }
}
