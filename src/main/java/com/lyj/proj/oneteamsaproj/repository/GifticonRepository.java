package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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


}
