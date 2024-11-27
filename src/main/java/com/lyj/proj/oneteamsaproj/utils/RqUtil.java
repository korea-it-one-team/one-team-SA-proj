package com.lyj.proj.oneteamsaproj.utils;

import java.io.IOException;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import com.lyj.proj.oneteamsaproj.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RqUtil {

    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public RqUtil(HttpServletRequest req, HttpServletResponse resp, MemberService memberService) {
        this.req = req;
        this.resp = resp;
        this.req.setAttribute("rq", this);
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
}
