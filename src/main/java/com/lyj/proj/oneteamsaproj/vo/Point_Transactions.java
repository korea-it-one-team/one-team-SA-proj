package com.lyj.proj.oneteamsaproj.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point_Transactions {
    private int id;
    private int user_id;
    private int Gifticon_id;
    private transactionType transaction_type; // "ADD" or "SUBTRACT"
    private LocalDateTime transaction_date;

    public enum transactionType {
        ADD, SUBTRACT
    }
}
