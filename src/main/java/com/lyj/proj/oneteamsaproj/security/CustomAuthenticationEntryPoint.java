package com.lyj.proj.oneteamsaproj.security;

import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private Rq rq;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        //로그인 되어서 거부된 경우 (anonymous여야 하는 경우)
        if (rq.isLogined()) {
            System.err.println("==================이미 로그인 상태 입니다.====================");

            rq.printHistoryBack("이미 로그인 상태 입니다.");
        } else {
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

    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(header);
    }
}