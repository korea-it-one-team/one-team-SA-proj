package com.lyj.proj.oneteamsaproj.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
정적 리소스 핸들러 설정

/resource/**로 시작하는 요청은 src/main/resources/static/resource/ 디렉토리에서 파일을 찾을수있도록
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.genFileDirPath}")
    private String genFileDirPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/gen/**").addResourceLocations("file:///" + genFileDirPath + "/")
                .setCachePeriod(20);

        registry.addResourceHandler("/resource/**")
                .addResourceLocations("classpath:/static/resource/");

        // 동적으로 서빙할 경로 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/static/images/")
                .setCachePeriod(0);  // 캐시 기간을 0으로 설정하여 파일 변경 즉시 반영

        // 동영상 경로 설정
        registry.addResourceHandler("/video/**")
                .addResourceLocations("file:src/main/resources/static/video/")
                .setCachePeriod(0);  // 캐시 기간을 0으로 설정
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS 허용 설정
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins("http://localhost:5000") // Flask 서버 허용
                .allowedOrigins("https://www.kicknalysis.ygcqwe.site") // 배포 서버 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowCredentials(true); // 인증 정보 허용 (필요 시)
    }
}