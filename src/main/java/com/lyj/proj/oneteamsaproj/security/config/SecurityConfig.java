package com.lyj.proj.oneteamsaproj.security.config;

import com.lyj.proj.oneteamsaproj.security.custom.CustomAuthenticationFilter;
import com.lyj.proj.oneteamsaproj.security.custom.CustomUserDetailsService;
import com.lyj.proj.oneteamsaproj.security.handler.CustomAccessDeniedHandler;
import com.lyj.proj.oneteamsaproj.security.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationFilter customAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            CustomAuthenticationFilter customAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.customAuthenticationFilter = customAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // AJAX 요청 허용
                        .requestMatchers(request -> "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))).permitAll()
                        .requestMatchers(
                                "/usr/article/write", "/usr/article/doWrite",
                                "/usr/article/modify", "/usr/article/doModify",
                                "/usr/article/doDelete", "/usr/gifticons/*/application",
                                "/usr/member/myPage", "/usr/member/checkPw",
                                "/usr/member/doCheckPw", "/usr/member/doLogout",
                                "/usr/member/modify", "/usr/member/doModify",
                                "/usr/reply/doWrite", "/usr/reactionPoint/doGoodReaction",
                                "/usr/reactionPoint/doBadReaction", "/predict"
                        ).authenticated()
                        .requestMatchers(
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/usr/member/findLoginId", "/usr/member/doFindLoginId",
                                "/usr/member/findLoginPw", "/usr/member/doFindLoginPw"
                        ).anonymous()
                        .requestMatchers("/adm/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint) // 인증되지 않은 사용자 처리
                        .accessDeniedHandler(accessDeniedHandler) // 권한 부족 처리
                )
                .formLogin(login -> login
                        .loginPage("/usr/member/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/usr/member/logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(request -> {
                            // CSRF를 비활성화할 조건
                            String requestUri = request.getRequestURI();
                            return requestUri.endsWith("Ajax") ||
                                    "Y".equals(request.getParameter("ajax")) ||
                                    "Y".equals(request.getParameter("isAjax"));
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/usr/member/login?error=sessionExpired")
                )
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 사용자 정의 필터 추가;

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
