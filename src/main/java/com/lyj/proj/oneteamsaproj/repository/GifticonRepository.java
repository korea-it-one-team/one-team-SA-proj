package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface GifticonRepository {

    @Select("""
            SELECT * FROM gifticons
            """)
    public List<Gifticon> findAll();



	@Select("""
        SELECT COUNT(*)
        FROM gifticons
        WHERE `name` LIKE CONCAT('%', #{searchKeyword}, '%')
        """)
	int getGifticonCount(String searchKeyword);

	@Select("""
        SELECT *
        FROM gifticons
        WHERE `name` LIKE CONCAT('%', #{searchKeyword}, '%')
        ORDER BY id DESC
        LIMIT #{limitFrom}, #{limitTake}
        """)
	List<Gifticon> getForPrintGifticons(int limitFrom, int limitTake, String searchKeyword);

	@Update("""
           		UPDATE gifticons
				set stock = stock + #{gifticonStock}
           		WHERE id = #{gifticonId}
           """)
	void stockAdd(int gifticonId, int gifticonStock);

	@Select("""
			select image_url from gifticon_Stock
			where gifticon_id = #{id}
			And `use` = 0
			order by id desc
			limit 1
			""")
	String getGifticonUrl(int id);

	@Update("""
			update gifticon_Stock
			set `use` = 1
			WHERE image_url = #{url}
			""")
	void useGifticon(String url);

	@Select("SELECT gifticon_id from exchange_history where id = #{id}")
	int getGifticonid(int id);

	@Update("UPDATE gifticons SET stock = stock - 1 WHERE id = ${gifticonId}")
	void stockuse(int gifticonId);
}
