package com.lyj.proj.oneteamsaproj.security.handler;

import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RqUtil rq;

    public CustomAuthenticationEntryPoint(RqUtil rq) {
        this.rq = rq;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (isLogined()) {
            // 이미 로그인 상태인 경우
            System.err.println("==================이미 로그인 상태 입니다.====================");
            rq.printHistoryBack("이미 로그인 상태 입니다.");
        } else {
            // 로그인되지 않은 경우
            System.err.println("==================로그인 후 이용해주세요.====================");
            String afterLoginUri = rq.getEncodedCurrentUri();

            if (isAjaxRequest(request)) {
                // AJAX 요청일 경우 JSON 응답
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"resultCode\":\"F-A\", \"msg\":\"로그인 후 이용해주세요\"}");
            } else {
                // 일반 요청일 경우 로그인 페이지로 리다이렉트
                rq.printReplace("F-A", "로그인 후 이용해주세요", "/usr/member/login?afterLoginUri=" + afterLoginUri);
            }
        }
    }

    /**
     * 로그인 상태 확인
     *
     * @return true if 로그인 상태, false otherwise
     */
    private boolean isLogined() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(header);
    }
}