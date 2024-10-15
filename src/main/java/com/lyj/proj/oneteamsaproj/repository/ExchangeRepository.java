package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import com.lyj.proj.oneteamsaproj.vo.Member;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ExchangeRepository {

	@Select("SELECT LAST_INSERT_ID();")
	public int getLastInsertId();

	@Select("""
            SELECT E.*, M.nickname AS nickname, G.name AS giftcon_Name  FROM `Exchange_History` AS E 
            inner join `Member` AS M
            SELECT E.*, M.nickName, G.name FROM exchange_history AS E
            INNER JOIN `member` AS M
            ON E.user_id = M.id
            INNER JOIN gifticons AS G
            ON E.gifticon_id = G.id
            """)
	public List<Exchange_History> gifticon_Application_List();

}