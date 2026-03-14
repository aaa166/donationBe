package com.chocobean.donation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 모든 경로에 대해 CORS 정책을 적용합니다.
                .allowedOrigins("http://localhost:5173") // 2. 🚨 리액트 앱의 주소(http://localhost:3000)를 허용합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 3. 허용할 HTTP 메서드를 지정합니다.
                .allowedHeaders("*") // 4. 모든 HTTP 헤더를 허용합니다.
                .allowCredentials(true); // 5. 쿠키/인증 정보 전송을 허용합니다.
    }
    // 이미지는 Cloudinary URL로 직접 접근하므로 로컬 ResourceHandler 불필요
}
