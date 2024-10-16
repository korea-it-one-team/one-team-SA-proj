package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import com.lyj.proj.oneteamsaproj.vo.Member;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExchangeRepository {

	@Select("SELECT LAST_INSERT_ID();")
	public int getLastInsertId();

	@Select("""
    <script>
        SELECT E.*, M.nickname AS nickname, G.name AS gifticon_Name  
        FROM `Exchange_History` AS E 
        INNER JOIN `member` AS M ON E.user_id = M.id
        INNER JOIN gifticons AS G ON E.gifticon_id = G.id
        WHERE 1 = 1
        <if test="status == 'REQUESTED' or status == 'COMPLETED'">
            AND E.exchange_status = #{status}
        </if>
        <if test="search != null and search != ''">
            AND (
                G.name LIKE CONCAT('%', #{search}, '%') 
                OR M.nickname LIKE CONCAT('%', #{search}, '%')
            )
        </if>
    </script>
""")
	public List<Exchange_History> gifticon_Application_List(
			@Param("search") String search,
			@Param("status") String status);


}