package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinDrawLose {

    private int id;
    private int gameId;
    private int memberId;
    private String prediction;

    // id를 제외한 생성자
    public WinDrawLose(int gameId, int memberId, String prediction) {
        this.gameId = gameId;
        this.memberId = memberId;
       this.prediction = prediction;
    }
}

