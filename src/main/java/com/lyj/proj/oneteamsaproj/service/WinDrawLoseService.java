package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.WinDrawLoseRepository;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class WinDrawLoseService {

    @Autowired
    private WinDrawLoseRepository winDrawLoseRepository;

    @Transactional
    public void savePrediction(WinDrawLose winDrawLose) {
        // 회원의 해당 게임에 대한 승/무/패 예측 정보 가져오기
        WinDrawLose existingPrediction = winDrawLoseRepository.findByGameIdAndMemberId(
                winDrawLose.getGameId(), winDrawLose.getMemberId());

        // 이미 존재한다면 DB에 추가 못하게
        if (existingPrediction != null) {
            throw new IllegalStateException("이미 해당 경기에 예측이 저장되어 있습니다.");
        }

        winDrawLoseRepository.insertPrediction(winDrawLose);
    }

    public List<WinDrawLose> getPrediction(int memberId) {
        return winDrawLoseRepository.findAllByMemberId(memberId);
    }
}

