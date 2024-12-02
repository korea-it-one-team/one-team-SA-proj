package com.lyj.proj.oneteamsaproj.security.custom;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomSessionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String sessionId = request.getHeader("X-Session-Id"); // 클라이언트에서 보낸 세션 ID 읽기
        if (sessionId != null) {
            HttpSession session = request.getSession(false); // 현재 세션 가져오기
            if (session != null && sessionId.equals(session.getId())) { // 세션 ID 일치 확인
                SecurityContext context = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                if (context != null) {
                    SecurityContextHolder.setContext(context); // SecurityContext 복원
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
