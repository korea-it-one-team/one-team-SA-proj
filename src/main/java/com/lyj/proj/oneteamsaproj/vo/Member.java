package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    private int id;
    private String regDate;
    private String updateDate;
    private String loginId;
    private String loginPw;
    private int authLevel;
    private String name;
    private String nickname;
    private String cellphoneNum;
    private String email;
  	private int points;
    private int delStatus;
    private String delDate;
    private LocalDateTime deletePendingDate;

    public String getForPrintType1RegDate() {
        return regDate.substring(2, 16).replace(" ", "<br />");
    }

    public String getForPrintType1UpdateDate() {
        return updateDate.substring(2, 16).replace(" ", "<br />");
    }

    public boolean isAdmin() {
        return this.authLevel == 7;
    }
}