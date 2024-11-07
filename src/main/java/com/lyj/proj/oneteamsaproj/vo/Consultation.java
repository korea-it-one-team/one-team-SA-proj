package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Consultation {
    private int id;
    private int member_id;
    private String title;
    private String content;
    private String status; // '대기중', '답변완료', '취소'
    private String answer;
}
