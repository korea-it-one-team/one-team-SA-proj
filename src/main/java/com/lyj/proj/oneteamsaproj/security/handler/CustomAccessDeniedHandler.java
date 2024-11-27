package com.lyj.proj.oneteamsaproj.security.handler;

import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private RqUtil rq;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException accessDeniedException) throws IOException {

        // 관리자 여부 확인
        boolean isAdmin = isAdmin();

        // AJAX 요청 여부 확인
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));

        String afterLoginUri = rq.getAfterLoginUri();

        if (isLogined()) {
            if (!isAdmin) {
                if (isAjax) {
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().append("{\"resultCode\":\"F-A\",\"msg\":\"관리자로 로그인 후 이용해주세요\"}");
                } else {
                    rq.jsReplace("관리자로 로그인 후 이용해주세요", "/adm/member/login?afterLoginUri=" + afterLoginUri);
                }
            }
        } else {
            if (isAjaxRequest(req)) {
                // AJAX 요청일 경우 JSON 응답
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"resultCode\":\"F-A\", \"msg\":\"로그인 후 이용해주세요.\"}");
            } else {
                // 일반 요청일 경우 로그인 페이지로 리다이렉트
                rq.printReplace("F-A", "로그인 후 이용해주세요.", "/usr/member/login?afterLoginUri=" + afterLoginUri);
            }
        }

    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(header);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        return false;
    }

    private boolean isLogined() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }
}
