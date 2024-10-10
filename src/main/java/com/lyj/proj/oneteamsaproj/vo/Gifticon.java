package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gifticon {
    private int id;
    private String name;
    private String description;
    private int points;
    private String image_url;
    private String created_at;

}
