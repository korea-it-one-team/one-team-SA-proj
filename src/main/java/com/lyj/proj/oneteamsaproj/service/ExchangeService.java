package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.ExchangeRepository;
import com.lyj.proj.oneteamsaproj.repository.GifticonRepository;
import com.lyj.proj.oneteamsaproj.vo.Exchange_Detail;
import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;

    public List<Exchange_History> getExchangeList(String search, String status) {
        // DB 조회 로직 구현 (필터 및 검색 조건 적용)
        System.out.println("sdfsdnk : " + status);
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
        System.out.println("zxcccc : " + exchange_status);
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
}
