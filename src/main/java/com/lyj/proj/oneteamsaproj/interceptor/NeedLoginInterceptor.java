package com.lyj.proj.oneteamsaproj.interceptor;

import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NeedLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private Rq rq;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        Rq rq = (Rq) req.getAttribute("rq");

        if (!rq.isLogined()) {
            System.err.println("==================로그인 후 이용해주세요.====================");

            String afterLoginUri = rq.getEncodedCurrentUri();

            rq.printReplace("F-A", "로그인 후 이용해주세요", "/usr/member/login?afterLoginUri=" + afterLoginUri);

            return false;

        }

        return HandlerInterceptor.super.preHandle(req, resp, handler);
    }
}
