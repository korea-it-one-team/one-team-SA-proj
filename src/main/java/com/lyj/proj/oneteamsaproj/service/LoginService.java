package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.security.custom.CustomUserDetails;
import com.lyj.proj.oneteamsaproj.vo.Member;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public LoginService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 사용자 인증 및 로그인 처리
     *
     * @param loginId 로그인 ID
     * @param loginPw 로그인 비밀번호
     * @return 로그인된 사용자 (Member)
     */
    public Member login(String loginId, String loginPw) {
        try {
            // AuthenticationManager로 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, loginPw)
            );

            // 인증 성공 시 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Authentication after login: " + SecurityContextHolder.getContext().getAuthentication());

            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession(false);
            if (session != null) {
                System.out.println("Session SecurityContext: " + session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
            } else {
                System.out.println("Session is null after login.");
            }

            // 로그인된 사용자 정보 반환
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                return ((CustomUserDetails) authentication.getPrincipal()).getMember();
            }
            throw new IllegalStateException("인증된 사용자 정보를 가져올 수 없습니다.");
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("로그인 실패: " + e.getMessage());
        }
    }

    public Member getLoginedMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getMember();
        }
        return null; // 로그인되지 않은 경우
    }

    public int getLoginedMemberId() {
        Member member = getLoginedMember();
        return member != null ? member.getId() : -1;
    }

    public void logout() {
        // Spring Security의 SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    /**
     * 현재 사용자가 로그인되어 있는지 확인
     *
     * @return true if 로그인된 상태, false otherwise
     */
    public boolean isLogined() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 인증 객체가 존재하고, 익명 사용자가 아닌 경우 로그인된 상태로 간주
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }
}
