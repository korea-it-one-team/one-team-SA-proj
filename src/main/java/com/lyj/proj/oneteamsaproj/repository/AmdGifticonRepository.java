package com.lyj.proj.oneteamsaproj.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AmdGifticonRepository {

    @Insert("""
                insert into gifticon_Stock
                set gifticon_id = #{gifticonId},
                image_url = #{uuid},
                created_at = NOW()        
            """)
    void gifticonStockSave(int gifticonId, String uuid);

    @Select("""
            SELECT COUNT(*) from gifticon_Stock
            WHERE gifticon_id = #{gifticon_id}
            """)
    int gifticonStockCount(int gifticonId);
}
