package com.lyj.proj.oneteamsaproj.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point_Transactions {
    private int user_id;
    private int point_id;
    private String transaction_type;
    private String transaction_date;
}
