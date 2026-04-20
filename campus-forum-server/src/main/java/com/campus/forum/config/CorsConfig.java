package com.campus.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 生产环境应该指定具体域名，而不是 *
                .allowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000")
                // 支持所有必要的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                // 允许所有请求头
                .allowedHeaders("*")
                // 允许携带凭证（Cookie、Authorization等）
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600)
                // 暴露的响应头
                .exposedHeaders("Authorization", "Content-Disposition");
    }
}