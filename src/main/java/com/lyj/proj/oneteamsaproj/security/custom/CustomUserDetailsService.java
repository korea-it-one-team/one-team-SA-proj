package com.lyj.proj.oneteamsaproj.security.custom;

import com.lyj.proj.oneteamsaproj.service.MemberService;
import com.lyj.proj.oneteamsaproj.vo.Member;
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

        // CustomUserDetails 객체를 반환
        return new CustomUserDetails(member);
    }
}