package com.lyj.proj.oneteamsaproj.security;

import com.lyj.proj.oneteamsaproj.service.MemberService;
import com.lyj.proj.oneteamsaproj.vo.Member;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    public CustomUserDetailsService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // MemberService를 사용해 사용자 정보를 가져옴
        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
        }

        // Spring Security의 UserDetails 객체 반환
        return User.builder()
                .username(member.getLoginId())
                .password(member.getLoginPw()) // 이미 암호화된 비밀번호
                .roles(member.isAdmin() ? "ADMIN" : "USER") // 권한 설정
                .build();
    }
}