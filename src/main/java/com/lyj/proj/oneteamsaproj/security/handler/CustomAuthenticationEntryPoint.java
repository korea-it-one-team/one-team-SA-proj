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

        System.out.println("CustomAuthenticationEntryPoint 실행");

        if (isLogined()) {
            System.out.println("isLogined() 진입");
            // 이미 로그인 상태인 경우
            System.err.println("==================이미 로그인 상태 입니다.====================");
            printHistoryBack(response, "이미 로그인 상태 입니다.");
        } else {
            System.out.println("!isLogined() 진입");
            // 로그인되지 않은 경우
            System.err.println("==================로그인 후 이용해주세요.====================");
            String afterLoginUri = rq.getEncodedCurrentUri();

            System.out.println("afterLoginUri = " + afterLoginUri);

            if (isAjaxRequest(request)) {
                System.out.println("isAjaxRequest(request) 진입");
                // AJAX 요청일 경우 JSON 응답
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"resultCode\":\"F-A\", \"msg\":\"로그인 후 이용해주세요\"}");
            } else {
                System.out.println("일반 요청 진입");
                // 일반 요청일 경우 로그인 페이지로 리다이렉트
                printReplace(response, "F-A", "로그인 후 이용해주세요", "/usr/member/login?afterLoginUri=" + afterLoginUri);
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

    private void printHistoryBack(HttpServletResponse resp, String msg) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().write("""
        <script>
            let msg = '%s';
            if (msg.length > 0) {
                alert(msg);
            }
            history.back();
        </script>
        """.formatted(msg));
        resp.getWriter().flush();
        resp.getWriter().close();
    }

    private void printReplace(HttpServletResponse resp, String resultCode, String msg, String replaceUri) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().write("""
        <script>
            let msg = '%s: %s';
            if (msg.length > 0) {
                alert(msg);
            }
            location.replace('%s');
        </script>
        """.formatted(resultCode, msg, replaceUri));
        resp.getWriter().flush();
        resp.getWriter().close();
    }
}