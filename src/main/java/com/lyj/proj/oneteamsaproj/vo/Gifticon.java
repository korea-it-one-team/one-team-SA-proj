package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gifticon {
    private int id;
    private String name;
    private int points;
    private String image_url;
    private int stock;
    private LocalDateTime created_at;

}
