package com.lyj.proj.oneteamsaproj.security;

import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private Rq rq;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException accessDeniedException) throws IOException {
        if (!rq.isAdmin()) {
            if (rq.isAjax()) {
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().append("{\"resultCode\":\"F-A\",\"msg\":\"관리자로 로그인 후 이용해주세요\"}");
            } else {
                String afterLoginUri = rq.getAfterLoginUri();
                rq.jsReplace("관리자로 로그인 후 이용해주세요", "/adm/member/login?afterLoginUri=" + afterLoginUri);
            }
        }
    }
}
