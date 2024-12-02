package com.lyj.proj.oneteamsaproj.configuration;

import com.lyj.proj.oneteamsaproj.security.custom.CustomUserDetails;
import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import com.lyj.proj.oneteamsaproj.vo.Member;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Component
@ControllerAdvice
public class GlobalControllerAdvice {

    private final RqUtil rqUtil;

    // 생성자 주입 방식으로 RqUtil 주입
    public GlobalControllerAdvice(RqUtil rqUtil) {
        this.rqUtil = rqUtil;
    }

    // RqUtil 객체를 Thymeleaf에 전달
    @ModelAttribute("rq")
    public RqUtil addRqUtilToModel() {
        return rqUtil;
    }

    @ModelAttribute("loginedMember")
    public Member addLoginedMemberToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMember();
        }
        return null; // 로그인되지 않은 경우 null
    }

    @ModelAttribute("loginedMemberId")
    public Integer addLoginedMemberIdToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMember().getId();
        }
        return -1; // 로그인되지 않은 경우 null
    }

    @ModelAttribute("isLogined")
    public boolean addIsLoginedToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }

    //Csrf 토큰
    @ModelAttribute("csrfToken")
    public CsrfToken addCsrfTokenToModel(CsrfToken csrfToken) {
        // Spring Security가 CsrfToken 객체를 자동으로 주입
        return csrfToken;
    }
}