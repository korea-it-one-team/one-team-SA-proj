package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    private int id;
    private String regDate;
    private String updateDate;
    private int memberId;
    private int boardId;
    private String title;
    private String body;
    private int hitCount;
    private int goodReactionPoint;
    private int badReactionPoint;

    private String extra__writer;

    private String extra__repliesCount;

    private String extra__sumReactionPoint;

    private boolean userCanModify;
    private boolean userCanDelete;

    // 게시글 작성시간이 현재시간 기준으로.
    private String formattedDate;

}