package com.campus.forum.config;

import com.campus.forum.security.JwtAuthenticationFilter;
import com.campus.forum.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
        http
                // 启用CORS并禁用CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 使用无状态Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置异常处理
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(
                                "/auth/wx/login",
                                "/auth/admin/login",
                                "/wx/**",
                                "/public/**",
                                "/uploads/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 论坛公开接口 - 查看帖子列表和详情
                        .requestMatchers(HttpMethod.GET,
                                "/forum/sections",
                                "/forum/sections/**",
                                "/forum/posts",
                                "/forum/posts/**")
                        .permitAll()

                        // 商品公开接口 - 查看商品列表和详情
                        .requestMatchers(HttpMethod.GET,
                                "/products",
                                "/products/**")
                        .permitAll()

                        // 失物招领公开接口
                        .requestMatchers(HttpMethod.GET,
                                "/lostfound",
                                "/lostfound/**")
                        .permitAll()

                        // 活动公开接口
                        .requestMatchers(HttpMethod.GET,
                                "/activities",
                                "/activities/**")
                        .permitAll()

                        // 互助公开接口
                        .requestMatchers(HttpMethod.GET,
                                "/help",
                                "/help/**")
                        .permitAll()

                        // 信息聚合公开接口
                        .requestMatchers(HttpMethod.GET,
                                "/info/**")
                        .permitAll()

                        // 统一搜索公开接口
                        .requestMatchers(HttpMethod.GET,
                                "/search",
                                "/search/**")
                        .permitAll()

                        // IM WebSocket 握手端点（握手阶段JWT校验）
                        .requestMatchers(
                                "/ws-im",
                                "/ws-im/**")
                        .permitAll()

                        // 轮播图公开接口
                        .requestMatchers(HttpMethod.GET,
                                "/banners",
                                "/banners/**")
                        .permitAll()

                        // ========== 管理员接口配置（修复 403 问题）==========
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 其他请求需要认证
                        .anyRequest().authenticated())
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}