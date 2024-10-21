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
	private String cellphoneNum;
	private String email;
	private int points;
	private boolean delStatus;
	private String delDate;
	

	public boolean isAdmin() {
		return this.authLevel == 7;
	}
	
   
}
