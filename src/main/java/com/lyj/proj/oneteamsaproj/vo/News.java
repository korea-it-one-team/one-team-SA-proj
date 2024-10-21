package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class News {

    private String title;
    private String link;
    private String imgUrl;
    private String desc;
    private String press;
    private String time;
}

