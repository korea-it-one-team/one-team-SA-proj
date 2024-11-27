package com.lyj.proj.oneteamsaproj.vo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.lyj.proj.oneteamsaproj.utils.Ut;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.lyj.proj.oneteamsaproj.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Rq {

    @Getter
    private boolean isAjax;

    private boolean isLoginedCached = false;
    private boolean isLoginedChecked = false;

    private Integer loginedMemberIdCached = null;
    private Member loginedMemberCached = null;

    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    private Map<String, String> paramMap;

    public Rq(HttpServletRequest req, HttpServletResponse resp, MemberService memberService) {
        this.req = req;
        this.resp = resp;
        this.memberService = memberService;

        this.paramMap = Ut.getParamMap(req);
        this.req.setAttribute("rq", this);

        // AJAX 요청 여부 판별
        String requestUri = req.getRequestURI();
        boolean isAjax = requestUri.endsWith("Ajax");

        if (!isAjax) {
            if (paramMap.containsKey("ajax") && paramMap.get("ajax").equals("Y")) {
                isAjax = true;
            } else if (paramMap.containsKey("isAjax") && paramMap.get("isAjax").equals("Y")) {
                isAjax = true;
            }
        }
        if (!isAjax) {
            if (requestUri.contains("/get")) {
                isAjax = true;
            }
        }
        this.isAjax = isAjax;
    }

    // Lazy loading 방식으로 세션에서 로그인 여부 확인
    public boolean isLogined() {
        if (!isLoginedChecked) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("loginedMemberId") != null) {
                isLoginedCached = true;
            }
            isLoginedChecked = true;
        }
        return isLoginedCached;
    }

    public int getLoginedMemberId() {
        if (loginedMemberIdCached == null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                loginedMemberIdCached = (Integer) session.getAttribute("loginedMemberId");
            }
            if (loginedMemberIdCached == null) {
                throw new IllegalStateException("로그인된 회원이 아닙니다.");
            }
        }
        return loginedMemberIdCached;
    }

    public Member getLoginedMember() {
        if (loginedMemberCached == null && isLogined()) {
            loginedMemberCached = memberService.getMemberById(getLoginedMemberId());
        }
        return loginedMemberCached;
    }

    public void logout() {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.removeAttribute("loginedMemberId");
            session.removeAttribute("loginedMember");
        }
        SecurityContextHolder.clearContext(); // Spring Security 인증 정보 제거
    }

    public void login(Member member) {
        HttpSession session = req.getSession(true);
        session.setAttribute("loginedMemberId", member.getId());
        session.setAttribute("loginedMember", member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getLoginId(),
                null, // 비밀번호는 저장하지 않음
                List.of(new SimpleGrantedAuthority(member.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void printHistoryBack(String msg) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        println("<script>");
        if (!Ut.isEmpty(msg)) {
            println("alert('" + msg + "');");
        }
        println("history.back();");
        println("</script>");
    }

    private void println(String str) {
        print(str + "\n");
    }

    private void print(String str) {
        try {
            resp.getWriter().append(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String historyBackOnView(String msg) {
        req.setAttribute("msg", msg);
        req.setAttribute("historyBack", true);
        return "/common/js";
    }

    public String getCurrentUri() {
        String currentUri = req.getRequestURI();
        String queryString = req.getQueryString();

        System.err.println(currentUri);
        System.err.println(queryString);

        if (currentUri != null && queryString != null) {
            currentUri += "?" + queryString;
        }

        System.out.println(currentUri);
        return currentUri;
    }

    public void printReplace(String resultCode, String msg, String replaceUri) {
        resp.setContentType("text/html; charset=UTF-8");
        print(Ut.jsReplace(resultCode, msg, replaceUri));

    }

    public String getEncodedCurrentUri() {
        return Ut.getEncodedCurrentUri(getCurrentUri());
    }

    public String getLoginUri() {
        return "../member/login?afterLoginUri=" + getAfterLoginUri();
    }

    public String getAfterLoginUri() {
        return getEncodedCurrentUri();
    }

    public String getImgUri(int id) {
        return "/common/genFile/file/article/" + id + "/extra/Img/1";
    }

    public String getVideoUri(int id) {
        return "/common/genFile/file/article/" + id + "/extra/Video/1";
    }

    public String getProfileFallbackImgUri() {
        return "https://via.placeholder.com/150/?text=*^_^*";
    }

    public String getProfileFallbackImgOnErrorHtml() {
        return "this.src = '" + getProfileFallbackImgUri() + "'";
    }

    public String getFindLoginIdUri() {
        return "../member/findLoginId?afterFindLoginIdUri=" + getAfterFindLoginIdUri();
    }

    private String getAfterFindLoginIdUri() {
        return "../member/login";
    }

    public String getFindLoginPwUri() {
        return "../member/findLoginPw?afterFindLoginPwUri=" + getAfterFindLoginPwUri();
    }

    private String getAfterFindLoginPwUri() {
        return "../member/login";
    }

    public String jsReplace(String msg, String uri) {
        return Ut.jsReplace(msg, uri);
    }

    public boolean isAdmin() {
        Member member = getLoginedMember();
        return member != null && member.isAdmin();
    }
}
