package com.lyj.proj.oneteamsaproj.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.lyj.proj.oneteamsaproj.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NeedLogoutInterceptor implements HandlerInterceptor {

	@Autowired
	private Rq rq;

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq.isLogined()) {
			System.err.println("==================로그인 상태입니다====================");

			rq.printHistoryBack("로그인 상태입니다");

			return false;

		}

		return HandlerInterceptor.super.preHandle(req, resp, handler);
	}

}
