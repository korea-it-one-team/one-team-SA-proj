package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.ExchangeRepository;
import com.lyj.proj.oneteamsaproj.repository.GifticonRepository;
import com.lyj.proj.oneteamsaproj.repository.MemberRepository;
import com.lyj.proj.oneteamsaproj.vo.Exchange_Detail;
import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private MemberRepository memberRepository;

    public List<Exchange_History> getExchangeList(String search, String status) {
        // DB 조회 로직 구현 (필터 및 검색 조건 적용)
        return exchangeRepository.gifticon_Application_List(search, status); // 예시로 빈 리스트 반환
    }

    public Exchange_History getExchangeById(int id) {
        // DB에서 교환 신청 내역 조회
        return new Exchange_History(); // 예시
    }

    public void updateExchangeStatus(int id, String status, String comment) {
        // 교환 상태 업데이트 로직 구현
        // 로그 기록 또는 알림 발송 처리 가능
    }

    public List<Exchange_Detail> findById(int id, int memberID) {
        return exchangeRepository.Exchange_History_detail(id,memberID);
    }

    public boolean completeExchange(int id) {
    String exchange_status = exchangeRepository.exchange_st(id);
    String status = "1";
        if (exchange_status.equals("REQUESTED")){
            status = "COMPLETED";
            exchangeRepository.exchange_Completed(id, status);
            return true;
        }else if (exchange_status.equals("COMPLETED")){
            status = "REQUESTED";
            exchangeRepository.exchange_Completed(id,status);
            return true;
        }
        return false;
    }

    public boolean gifticon_Application(int id, int memberId) {

        if (memberId <= 0) {
            return false;
        };
        exchangeRepository.gifticon_Application(id, memberId);
        return true;
    }

    public boolean getGifticonPoint(int id, int member_points, int member_id) {

        int point = exchangeRepository.getExchangeGifticonPoint(id);

        if(point <= member_points){
            memberRepository.modifyPoint(point,member_id);
            return true;
        }

        return false;
    }

    public String getPhoneNum(int id) {
        return exchangeRepository.getPhoneNum(id);
    }
}
