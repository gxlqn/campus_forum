package com.campus.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园服务论坛系统启动类
 *
 * 注意：@MapperScan 注解暂时移除，待 MyBatis-Plus 依赖下载后可恢复
 * 或在 application.yml 中配置 mybatis-plus.mapper-locations
 */
@SpringBootApplication
@EnableScheduling
public class CampusForumServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusForumServerApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  校园服务平台启动成功！");
        System.out.println("  API文档地址: http://localhost:8080/api/doc.html");
        System.out.println("==========================================");
    }

}
