package com.lyj.proj.oneteamsaproj.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exchange_History {
    private int id;
    private int member_id;
    private String nickname;
    private int gifticon_id;
    private String gifticon_Name;
    private int points;
    private ExchangeStatus exchange_status; // REQUESTED,  COMPLETED
    private LocalDateTime exchange_date;

    public enum ExchangeStatus {
        REQUESTED,  COMPLETED
    }
}

