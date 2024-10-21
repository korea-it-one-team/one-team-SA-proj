package com.lyj.proj.oneteamsaproj.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exchange_Detail {
    private int exchange_Id;
    private String gifticon_Name;
    private String exchange_Status;
    private String member_Name;
    private String member_Phone;
}
