package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.LoginService;
import com.lyj.proj.oneteamsaproj.service.MemberService;
import com.lyj.proj.oneteamsaproj.utils.PasswordHelper;
import com.lyj.proj.oneteamsaproj.utils.Ut;
import com.lyj.proj.oneteamsaproj.vo.Member;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsrMemberController {

    @Autowired
    private RqUtil rq;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private PasswordHelper passwordHelper;

    @RequestMapping("/usr/member/doLogout")
    @ResponseBody
    public String doLogout(HttpServletRequest req) {

        loginService.logout();

        return Ut.jsReplace("S-1", Ut.f("로그아웃"), "/");
    }

    @RequestMapping("/usr/member/login")
    public String showLogin(@RequestParam(value = "error", required = false) String error, HttpServletRequest req) {
        if ("sessionExpired".equals(error)) {
            return Ut.jsReplace("F-1","세션이 만료되었습니다. 다시 로그인해주세요.", "usr/member/login");
        }
        return "usr/member/login";
    }


    @RequestMapping("/usr/member/doLogin")
    @ResponseBody
    public String doLogin(HttpServletRequest req, String loginId, String loginPw, String afterLoginUri) {

        if (Ut.isEmptyOrNull(loginId)) {
            return Ut.jsHistoryBack("F-2", "아이디를 입력하지 않았습니다.");
        }
        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-3", "비밀번호를 입력하지 않았습니다.");
        }

        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            return Ut.jsHistoryBack("F-3", Ut.f("%s는(은) 존재하지 않는 아이디 입니다.", loginId));
        }

        if (!passwordHelper.isPasswordMatch(loginPw, member.getLoginPw())) {
            return Ut.jsHistoryBack("F-4", "비밀번호가 일치하지 않습니다.");
        }

        if (member.getDelStatus() == 2) {
            return Ut.jsReplace("사용정지된 계정입니다.", "/");
        }

        // 로그인 처리
        try {
            // LoginService를 통해 로그인 처리
            member = loginService.login(loginId, loginPw);
        } catch (IllegalArgumentException e) {
            return Ut.jsHistoryBack("F-4", e.getMessage());
        }

        if (member.getDelStatus() == 2) {
            return Ut.jsReplace("사용정지된 계정입니다.", "/");
        }

        // 로그인 후 이동할 URL 처리
        if (!Ut.isEmptyOrNull(afterLoginUri)) {
            return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다", member.getNickname()), afterLoginUri);
        }

        return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다.", member.getNickname()), "/");
    }

    @RequestMapping("/usr/member/join")
    public String showJoin(HttpServletRequest req) {

        return "usr/member/join";
    }

    @RequestMapping("/usr/member/doJoin")
    @ResponseBody
    public String doJoin(HttpServletRequest req, String loginId, String loginPw, String name, String nickname,
                         String cellphoneNum, String email) {

        if (Ut.isEmptyOrNull(loginId)) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d(을)를 제대로 입력해주세요.", loginId));
        }

        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-2", Ut.f("%d(을)를 제대로 입력해주세요.", loginPw));
        }
        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-3", Ut.f("%d(을)를 제대로 입력해주세요.", name));
        }
        if (Ut.isEmptyOrNull(nickname)) {
            return Ut.jsHistoryBack("F-4", Ut.f("%d(을)를 제대로 입력해주세요.", nickname));
        }
        if (Ut.isEmptyOrNull(cellphoneNum)) {
            return Ut.jsHistoryBack("F-5", Ut.f("%d(을)를 제대로 입력해주세요.", cellphoneNum));
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", Ut.f("%d(을)를 제대로 입력해주세요.", email));
        }

        ResultData joinRd = memberService.join(loginId, loginPw, name, nickname, cellphoneNum, email);

        if (joinRd.isFail()) {
            return Ut.jsHistoryBack(joinRd.getResultCode(), joinRd.getMsg());
        }

        Member member = memberService.getMemberById((int) joinRd.getData1());

        return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../member/login");

    }

    @RequestMapping("/usr/member/myPage")
    public String showmyPage() {
        return "usr/member/myPage";
    }

    @RequestMapping("/usr/member/checkPw")
    public String showCheckPw() {
        return "usr/member/checkPw";
    }

    @RequestMapping("/usr/member/doCheckPw")
    @ResponseBody
    public String doCheckPw(String loginPw) {

        Member member = loginService.getLoginedMember();

        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-1", "비밀번호를 입력해주세요.");
        }

        if (!passwordHelper.isPasswordMatch(loginPw, member.getLoginPw())) {
            return Ut.jsHistoryBack("F-2", "비밀번호가 틀렸습니다.");
        }

        return Ut.jsReplace("S-1", Ut.f("비밀번호 확인 성공"), "modify");
    }

    @RequestMapping("/usr/member/modify")
    public String showModify() {
        return "usr/member/modify";
    }

    @RequestMapping("/usr/member/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, String loginPw, String name, String nickname, String cellphoneNum, String email) {

        // 비번을 입력하지 않아도 회원정보 수정이 가능하도록 만들어야 함.(비번은 바꾸기 싫을때.)
        // 비번은 안바꾸는거 가능(사용자 입장). 비번 null 체크 X

        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-3", Ut.f("%d(을)를 제대로 입력해주세요.", name));
        }
        if (Ut.isEmptyOrNull(nickname)) {
            return Ut.jsHistoryBack("F-4", Ut.f("%d(을)를 제대로 입력해주세요.", nickname));
        }
        if (Ut.isEmptyOrNull(cellphoneNum)) {
            return Ut.jsHistoryBack("F-5", Ut.f("%d(을)를 제대로 입력해주세요.", cellphoneNum));
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", Ut.f("%d(을)를 제대로 입력해주세요.", email));
        }

        ResultData modifyRd;

        if (Ut.isEmptyOrNull(loginPw)) {
            modifyRd = memberService.modifyWithoutPw(loginService.getLoginedMemberId(), name, nickname, cellphoneNum, email);

        } else {
            modifyRd = memberService.modify(loginService.getLoginedMemberId(), loginPw, name, nickname, cellphoneNum, email);
        }

        if(modifyRd.isSuccess()) {
            memberService.updateMemberInfo(loginService.getLoginedMemberId());
        }

        return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../member/myPage");
    }

    @RequestMapping("/usr/member/getLoginIdDup")
    @ResponseBody
    public ResultData getLoginIdDup(String loginId) {

        if (Ut.isEmpty(loginId)) {
            return ResultData.from("F-1", "아이디를 입력해주세요.");
        }

        Member existsMember = memberService.getMemberByLoginId(loginId);

        if (existsMember != null) {
            return ResultData.from("F-2", "이미 사용중인 아이디입니다.", "loginId", loginId);
        }

        return ResultData.from("S-1", "사용 가능한 아이디입니다.", "loginId", loginId);
    }

    @RequestMapping("/usr/member/findLoginId")
    public String showFindLoginId() {

        return "usr/member/findLoginId";
    }

    @RequestMapping("/usr/member/doFindLoginId")
    @ResponseBody
    public String doFindLoginId(@RequestParam(defaultValue = "/") String afterFindLoginIdUri, String name,
                                String email) {

        Member member = memberService.getMemberByNameAndEmail(name, email);

        if (member == null) {
            return Ut.jsHistoryBack("F-1", "존재하지 않는 유저 정보입니다.");
        }

        return Ut.jsReplace("S-1", Ut.f("회원님의 아이디는 [ %s ] 입니다.", member.getLoginId()), afterFindLoginIdUri);
    }

    @RequestMapping("/usr/member/findLoginPw")
    public String showFindLoginPw() {

        return "usr/member/findLoginPw";
    }

    @RequestMapping("/usr/member/doFindLoginPw")
    @ResponseBody
    public String doFindLoginPw(@RequestParam(defaultValue = "/") String afterFindLoginPwUri, String loginId,
                                String email) {

        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            return Ut.jsHistoryBack("F-1", "존재하지 않는 유저 정보입니다.");
        }

        if (member.getEmail().equals(email) == false) {
            return Ut.jsHistoryBack("F-2", "일치하는 이메일이 없습니다.");
        }

        ResultData notifyTempLoginPwByEmailRd = memberService.notifyTempLoginPwByEmail(member);

        return Ut.jsReplace(notifyTempLoginPwByEmailRd.getResultCode(), notifyTempLoginPwByEmailRd.getMsg(),
                afterFindLoginPwUri);
    }

    // 회원이 직접 탈퇴처리
    @RequestMapping("/usr/member/doDelete")
    public String doDelete() {
        int loginedMemberId = loginService.getLoginedMemberId();

        // 7일 유예 기간 설정
        memberService.doDeleteMember(loginedMemberId, 7);

        return "redirect:/usr/member/myPage";
    }

    // 회원이 탈퇴처리 취소
    @RequestMapping("/usr/member/doRestore")
    public String doRestore() {
        int loginedMemberId = loginService.getLoginedMemberId();

        memberService.restoreMember(loginedMemberId);

        return "redirect:/usr/member/myPage";
    }

}
