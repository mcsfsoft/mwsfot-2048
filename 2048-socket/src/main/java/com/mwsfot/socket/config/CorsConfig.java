package com.mwsfot.socket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author MinChang
 * @description 待完善
 * @date 2024/5/24 17:06
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //支持所有接口
        registry.addMapping("/**")
            //是否发送Cookie
            .allowCredentials(true)
            //支持域
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .exposedHeaders("*");
    }
}
