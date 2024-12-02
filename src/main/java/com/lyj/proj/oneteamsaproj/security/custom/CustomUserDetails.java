package com.lyj.proj.oneteamsaproj.security.custom;

import com.lyj.proj.oneteamsaproj.vo.Member;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class CustomUserDetails extends User {

    private final Member member;

    public CustomUserDetails(Member member) {
        super(
                member.getLoginId(), // 로그인 ID
                member.getLoginPw(), // 비밀번호
                List.of(new SimpleGrantedAuthority(
                        member.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER" // 권한 설정
                ))
        );
        this.member = member;
    }

    /**
     * Spring Security에서 사용할 수 있도록 제공한 Member 객체를 반환.
     *
     * @return 원본 Member 객체
     */
    public Member getMember() {
        return member;
    }

    /**
     * ID 반환 (Thymeleaf나 다른 컨트롤러에서 사용 가능)
     *
     * @return 로그인된 사용자의 ID
     */
    public int getId() {
        return member.getId();
    }

    /**
     * 닉네임 반환
     *
     * @return 로그인된 사용자의 닉네임
     */
    public String getNickname() {
        return member.getNickname();
    }

    /**
     * 이메일 반환
     *
     * @return 로그인된 사용자의 이메일
     */
    public String getEmail() {
        return member.getEmail();
    }

    /**
     * 포인트 반환
     *
     * @return 로그인된 사용자의 포인트
     */
    public int getPoints() {
        return member.getPoints();
    }

    /**
     * 삭제 여부 확인
     *
     * @return true if 삭제 상태, false otherwise
     */
    public boolean isDeleted() {
        return member.getDelStatus() != 0;
    }

    /**
     * 관리자 여부 확인 (기존 isAdmin 메서드 사용)
     *
     * @return true if 관리자, false otherwise
     */
    public boolean isAdmin() {
        return member.isAdmin();
    }
}
