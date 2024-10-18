package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Exchange_Detail;
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
			        INNER JOIN `member` AS M ON E.member_id = M.id
			        <if test="status == 'REQUESTED' or status == 'COMPLETED'">
			        	AND E.exchange_status = #{status}
			        </if>
			        INNER JOIN gifticons AS G ON E.gifticon_id = G.id
			        WHERE 1
			        <if test="search != null and search != ''">
			            AND G.name LIKE CONCAT('%', #{search}, '%') 
			            OR M.nickname LIKE CONCAT('%', #{search}, '%')
			        </if>
			    </script>
			""")
	public List<Exchange_History> gifticon_Application_List(
			@Param("search") String search,
			@Param("status") String status);

	@Select("""
			SELECT E.id AS exchange_Id, G.name AS gifticon_Name, M.name AS member_Name, M.cellphoneNum AS member_Phone, E.exchange_status AS exchange_Status FROM `Exchange_History` AS E
			inner join `member` AS M ON E.member_id = M.id
			inner join `gifticons` AS G ON G.id = E.gifticon_id
			where E.id = #{id}
			""")
	public List<Exchange_Detail> Exchange_History_detail(int id, int memberID);

	@Select("SELECT exchange_status FROM exchange_history WHERE id = #{id}")
	public String exchange_st(int id);

	@Update("""
		    	UPDATE exchange_history
		    	SET exchange_status = #{status}
		    	WHERE id = #{id}
			""")
	void exchange_Completed(@Param("id") int id, @Param("status") String status);

	@Insert("""
				insert into exchange_history
				set member_id = ${member_Id},
	    		gifticon_id = ${gifticon_Id},
	    		exchange_status = 'REQUESTED',
	    		exchange_date = now(),
	    		points = Select points from gifticons where id = ${id}
			""")
	void gifticon_Application(int id, int memberId);

	@Select("""
				SELECT poins FROM gifticons WHERE id = #{id}
			""")
	public int getExchangeGifticonPoint(int id);
}