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

    private final RqUtil rq;

    public CustomAccessDeniedHandler(RqUtil rq) {
        this.rq = rq;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException accessDeniedException) throws IOException {

        System.out.println("AccessDeniedHandler 호출됨"); // 디버깅 로그 추가
        System.out.println("rq : " + rq);

        // CSRF 예외인지 확인
        if (accessDeniedException instanceof org.springframework.security.web.csrf.CsrfException) {
            handleCsrfException(req, resp);
            return; // CSRF 관련 예외 처리 후 나머지 로직은 실행되지 않도록 반환
        }

        // 관리자 여부 확인
        boolean isAdmin = isAdmin();
        System.out.println("isAdmin = " + isAdmin);

        // AJAX 요청 여부 확인
        boolean isAjax = isAjaxRequest(req);
        System.out.println("isAjax = " + isAjax);

        String afterLoginUri = rq.getAfterLoginUri();
        System.out.println("afterLoginUri = " + afterLoginUri);

        System.out.println("isLogined = " + isLogined());

        if (!isLogined()) {
            // 인증되지 않은 사용자 처리
            System.out.println("!isLogined() 진입");
            if (isAjax) {
                System.out.println("isAjax 진입");
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"resultCode\":\"F-A\", \"msg\":\"로그인 후 이용해주세요.\"}");
            } else {
                System.out.println("일반 요청 진입");
                printReplace(resp, "F-A", "로그인 후 이용해주세요.", "/usr/member/login?afterLoginUri=" + afterLoginUri);
            }
            return;
        }

        if (!isAdmin) {
            // 인증되었지만 권한 부족
            System.out.println("!isAdmin 진입");
            if (isAjax) {
                System.out.println("isAjax 진입");
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"resultCode\":\"F-A\", \"msg\":\"관리자로 로그인 후 이용해주세요.\"}");
            } else {
                System.out.println("일반 요청 진입");
                jsReplace(resp,"관리자로 로그인 후 이용해주세요", "/usr/member/login?afterLoginUri=" + afterLoginUri);
            }
            return;
        }

        System.out.println("기타 AccessDeniedException");

        // 기타 AccessDeniedException 처리
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }

    /**
     * CSRF 예외 처리 메서드
     */
    private void handleCsrfException(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean isAjax = isAjaxRequest(req);

        if (isAjax) {
            // AJAX 요청인 경우 JSON 응답
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"resultCode\":\"F-CSRF\", \"msg\":\"유효하지 않은 CSRF 토큰입니다.\"}");
        } else {
            // 일반 요청인 경우 에러 알림
            jsAlert(resp, "F-CSRF", "유효하지 않은 CSRF 토큰입니다. 다시 시도해주세요.");
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(header);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isLogined() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
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

    private void jsReplace(HttpServletResponse resp, String msg, String replaceUri) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().write("""
        <script>
            let msg = '%s';
            if (msg.length > 0) {
                alert(msg);
            }
            location.replace('%s');
        </script>
        """.formatted(msg, replaceUri));
        resp.getWriter().flush();
        resp.getWriter().close();
    }

    private void jsAlert(HttpServletResponse resp, String resultCode, String msg) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().write("""
        <script>
            let msg = '%s: %s';
            alert(msg);
        </script>
        """.formatted(resultCode, msg));
        resp.getWriter().flush();
        resp.getWriter().close();
    }
}
