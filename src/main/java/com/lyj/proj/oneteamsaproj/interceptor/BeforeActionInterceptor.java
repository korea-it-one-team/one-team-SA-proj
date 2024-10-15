package com.lyj.proj.oneteamsaproj.interceptor;

import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BeforeActionInterceptor implements HandlerInterceptor {

    @Autowired
    private Rq rq;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {

//		Rq rq = new Rq(req, resp);

//		req.setAttribute("rq", rq);

        rq.initBeforeActionInterceptor();

        return HandlerInterceptor.super.preHandle(req, resp, handler);
    }
}
