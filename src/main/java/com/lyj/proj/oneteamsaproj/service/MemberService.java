package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.MemberRepository;
import com.lyj.proj.oneteamsaproj.utils.Ut;
import com.lyj.proj.oneteamsaproj.vo.Member;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberService {

    @Value("${custom.siteMainUri}")
    private String siteMainUri;

    @Value("${custom.siteName}")
    private String siteName;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MailService mailService;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public ResultData<Integer> join(String loginId, String loginPw, String name, String nickname, String cellphoneNum, String email) {

        Member existsMember = getMemberByLoginId(loginId);

        if (existsMember != null) {
            return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다.", loginId));
        }

        existsMember = getMemberByNameAndEmail(name, email);

        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 이메일(%s)입니다.", name, email));
        }

        loginPw = Ut.sha256(loginPw);

        memberRepository.doJoin(loginId, loginPw, name, nickname, cellphoneNum, email);

        int id = memberRepository.getLastInsertId();

        return ResultData.from("S-1", "회원가입 성공!", "생성된 회원 id", id);
    }

    public ResultData<Integer> sign(String loginId, String loginPw, String name, String cellphoneNum, String email) {
        Member existsMember = getMemberByLoginId(loginId);

        if (existsMember != null) {
            return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다.", loginId));
        }

        existsMember = getMemberByNameAndEmail(name, email);

        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 이메일(%s)입니다.", name, email));
        }

        loginPw = Ut.sha256(loginPw);
        memberRepository.doSign(loginId, loginPw, name, cellphoneNum, email);

        int id = memberRepository.getLastInsertId();

        return ResultData.from("S-1", "회원가입 성공", "생성된 회원 id", id);
    }

    public int getMembersCount(String authLevel, String searchKeywordTypeCode, String searchKeyword) {
        return memberRepository.getMembersCount(authLevel, searchKeywordTypeCode, searchKeyword);
    }

    public List<Member> getForPrintMembers(String authLevel, String searchKeywordTypeCode, String searchKeyword, int itemsInAPage, int page) {
        int limitStart = (page - 1) * itemsInAPage;
        int limitTake = itemsInAPage;

        return memberRepository.getForPrintMembers(authLevel, searchKeywordTypeCode, searchKeyword, limitStart, limitTake);
    }

    public Member getMemberByNameAndEmail(String name, String email) {
        return memberRepository.getMemberByNameAndEmail(name, email);
    }

    public Member getMemberByLoginId(String loginId) {
        return memberRepository.getMemberByLoginId(loginId);
    }

    public Member getMemberById(int id) {
        return memberRepository.getMemberById(id);
    }

    public ResultData modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphoneNum, String email) {
        loginPw = Ut.sha256(loginPw);
        memberRepository.modify(loginedMemberId, loginPw, name, nickname, cellphoneNum, email);

        return ResultData.from("S-1", "회원정보 수정 완료");
    }

    public ResultData modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphoneNum, String email) {
        memberRepository.modifyWithoutPw(loginedMemberId, name, nickname, cellphoneNum, email);

        return ResultData.from("S-1", "회원정보 수정 완료");
    }

    public void deleteMembers(List<Integer> memberIds) {
        for (int memberId : memberIds) {
            Member member = getMemberById(memberId);
            if (member != null) {
                deleteMember(member);
            }
        }
    }

    // 관리자가 탈퇴
    private void deleteMember(Member member) {
        memberRepository.deleteMember(member.getId());
    }

    public ResultData notifyTempLoginPwByEmail(Member actor) {
        String title = "[" + siteName + "] 임시 패스워드 발송";
        String tempPassword = Ut.getTempPassword(6);
        String body = "<h1>임시 패스워드 : " + tempPassword + "</h1>";
        body += "<a href=\"" + siteMainUri + "/usr/member/login\" target=\"_blank\">로그인 하러가기</a>";

        ResultData sendResultData = mailService.send(actor.getEmail(), title, body);

        if (sendResultData.isFail()) {
            return sendResultData;
        }

        setTempPassword(actor, tempPassword);

        return ResultData.from("S-1", "계정의 이메일주소로 임시 패스워드가 발송되었습니다.");
    }

    private void setTempPassword(Member actor, String tempPassword) {
        memberRepository.modify(actor.getId(), Ut.sha256(tempPassword), null, null, null, null);
    }

    public void addPoints(int memberId, int points) {
        memberRepository.addPoints(memberId, points); // 승부 예측 성공시 포인트 +5 추가 메서드
    }

    // 회원이 직접 탈퇴처리
    public void doDeleteMember(int memberId, int gracePeriodDays) {
        Member member = memberRepository.getMemberById(memberId);
        if (member == null || member.getDelStatus() == 1 || member.getDelStatus() == 2) {
            throw new IllegalStateException("존재하지 않거나 이미 삭제된 회원입니다.");
        }
        memberRepository.doDeleteMember(memberId, gracePeriodDays);
    }

    // 회원이 탈퇴처리 취소
    public void restoreMember(int memberId) {
        Member member = memberRepository.getMemberById(memberId);
        if (member == null || member.getDelStatus() != 1) {
            throw new IllegalStateException("존재하지 않거나 정지되지 않은 회원입니다.");
        }
        if (member.getDeletePendingDate() != null && member.getDeletePendingDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("탈퇴 유예 기간이 만료되어 복구할 수 없습니다.");
        }

        int affectedRows = memberRepository.restoreMember(memberId);
        if (affectedRows == 0) {
            throw new IllegalStateException("회원 복구에 실패했습니다.");
        }
    }

    public int deleteExpiredMembers() {
        return memberRepository.markMembersAsUnrecoverable();
    }
}
