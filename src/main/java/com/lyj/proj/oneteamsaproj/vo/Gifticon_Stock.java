package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gifticon_Stock {
    private int id;
    private int gifticon_id;
    private String image_url;
    private MultipartFile image;
    private LocalDateTime created_at;

}
