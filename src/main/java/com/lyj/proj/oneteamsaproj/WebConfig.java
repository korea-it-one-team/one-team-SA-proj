package com.lyj.proj.oneteamsaproj;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
정적 리소스 핸들러 설정

/resource/**로 시작하는 요청은 src/main/resources/static/resource/ 디렉토리에서 파일을 찾을수있도록
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resource/**")
                .addResourceLocations("classpath:/static/resource/");
    }
}