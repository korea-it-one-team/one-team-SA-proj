package com.lyj.proj.oneteamsaproj.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exchange_Logs {
    private int id;
    private int exchange_id;
    private int member_id;
    private int gifticon_id;
    private LocalDateTime log_date;
}
