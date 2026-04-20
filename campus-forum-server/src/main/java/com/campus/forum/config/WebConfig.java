package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用 toUri().toString() 生成正确规范的 file:/// URL，避免 Windows 盘符路径解析错误
        String resourceLocation = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        // 配置静态资源访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // 打印配置信息，方便调试
        System.out.println("=== 静态资源配置 ===");
        System.out.println("配置路径: " + uploadPath);
        System.out.println("访问路径: /uploads/** -> " + resourceLocation);
    }
}