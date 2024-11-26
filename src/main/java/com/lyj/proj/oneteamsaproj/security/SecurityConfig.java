package com.lyj.proj.oneteamsaproj.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Value("${custom.genFileDirPath}")
    private String genFileDirPath;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 로그인 필요 경로
                        .requestMatchers(
                                "/usr/article/write", "/usr/article/doWrite",
                                "/usr/article/modify", "/usr/article/doModify",
                                "/usr/article/doDelete", "/usr/gifticons/**/application",
                                "/usr/member/myPage", "/usr/member/checkPw",
                                "/usr/member/doCheckPw", "/usr/member/doLogout",
                                "/usr/member/modify", "/usr/member/doModify",
                                "/usr/reply/doWrite", "/usr/reactionPoint/doGoodReaction",
                                "/usr/reactionPoint/doBadReaction", "/predict"
                        ).authenticated()
                        // 로그아웃 필요 경로
                        .requestMatchers(
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/usr/member/findLoginId", "/usr/member/doFindLoginId",
                                "/usr/member/findLoginPw", "/usr/member/doFindLoginPw"
                        ).anonymous()
                        // 관리자 권한 필요
                        .requestMatchers("/adm/**").hasRole("ADMIN")
                        // 기타 경로
                        .anyRequest().permitAll()
                )
                // 로그인 설정
                .formLogin(login -> login
                        .loginPage("/usr/member/login") // 커스텀 로그인 페이지 경로
                        .permitAll()
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/usr/member/logout") // 로그아웃 처리 경로
                        .permitAll()
                );

        return http.build();
    }
}
