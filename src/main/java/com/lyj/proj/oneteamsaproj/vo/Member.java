package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private boolean delStatus;
    private String delDate;

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
