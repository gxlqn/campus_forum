package com.campus.forum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 全局日期格式配置
 *
 * 解决前端传 "2026-04-16 19:34:00" 格式时，
 * LocalDateTime 反序列化失败的问题。
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        JsonDeserializer<LocalDateTime> compatibleLocalDateTimeDeserializer =
                new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser p,
                            com.fasterxml.jackson.databind.DeserializationContext ctxt)
                            throws java.io.IOException {
                        String text = p.getText();
                        if (text == null || text.trim().isEmpty()) {
                            return null;
                        }
                        String value = text.trim();
                        try {
                            return LocalDateTime.parse(value, formatter);
                        } catch (DateTimeParseException ignore) {
                            try {
                                return LocalDateTime.parse(value, isoFormatter);
                            } catch (DateTimeParseException ex) {
                                throw ctxt.weirdStringException(value, LocalDateTime.class,
                                        "支持格式: yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd'T'HH:mm:ss");
                            }
                        }
                    }
                };

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, compatibleLocalDateTimeDeserializer);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));

        // 不自动发现和注册模块，避免与默认配置冲突
        builder.modules(javaTimeModule);
        return builder.build();
    }
}
