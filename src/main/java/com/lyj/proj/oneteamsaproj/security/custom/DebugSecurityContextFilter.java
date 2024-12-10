package com.lyj.proj.oneteamsaproj.security.custom;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import java.io.IOException;

public class DebugSecurityContextFilter extends SecurityContextPersistenceFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 기존 세션 확인
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            System.out.println("Existing Session ID: " + session.getId());
        } else {
            System.out.println("No existing session found. Will create a new one if required.");
        }

        // SecurityContext 확인
        System.out.println("Before SecurityContextPersistenceFilter - SecurityContext: "
                + SecurityContextHolder.getContext());

        super.doFilter(request, response, chain);

        // 요청 이후 SecurityContext 확인
        System.out.println("After SecurityContextPersistenceFilter - SecurityContext: "
                + SecurityContextHolder.getContext());

        HttpSession newSession = httpRequest.getSession(false);
        if (newSession != null) {
            System.out.println("After Filter - Session ID: " + newSession.getId());
        } else {
            System.out.println("After Filter - No session created.");
        }
    }
}