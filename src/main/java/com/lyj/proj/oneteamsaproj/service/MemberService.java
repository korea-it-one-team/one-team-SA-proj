package com.lyj.proj.oneteamsaproj.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lyj.proj.oneteamsaproj.Util.Ut;
import com.lyj.proj.oneteamsaproj.repository.MemberRepository;
import com.lyj.proj.oneteamsaproj.vo.Member;
import com.lyj.proj.oneteamsaproj.vo.ResultData;



@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public ResultData<Integer> sign(String loginId, String loginPw, String name, String cellphoneNum,
			String email) {
		

		Member existsMember = getMemberByLoginId(loginId);
		System.out.print("existsMember1 : " + existsMember);
		if (existsMember != null) {
			return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다.", loginId));
		}

		existsMember = getMemberByNameAndEmail(name, email);
		System.out.print("existsMember2 : " + existsMember);
		if (existsMember != null) {
			return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 이메일(%s)입니다.", name, email));
		}
		loginPw = Ut.sha256(loginPw);
		memberRepository.doSign(loginId, loginPw, name, cellphoneNum, email);

		int id = memberRepository.getLastInsertId();
		System.out.print("id1 : " + id);
		return ResultData.from("S-1", "회원가입 성공", "생성된 회원 id", id);
	}

	private Member getMemberByNameAndEmail(String name, String email) {
		return memberRepository.getMemberByNameAndEmail(name, email);
	}

	public Member getMemberByLoginId(String loginId) {
		return memberRepository.getMemberByLoginId(loginId);
	}

	public Member getMemberById(int id) {
		return memberRepository.getMemberById(id);
	}
	/*

	public ResultData modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphoneNum,
			String email) {

		memberRepository.modify(loginedMemberId, loginPw, name, nickname, cellphoneNum, email);

		return ResultData.from("S-1", "회원정보 수정 완료");
	}

	public ResultData modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphoneNum,
			String email) {
		memberRepository.modifyWithoutPw(loginedMemberId, name, nickname, cellphoneNum, email);

		return ResultData.from("S-1", "회원정보 수정 완료");
	}
*/
}