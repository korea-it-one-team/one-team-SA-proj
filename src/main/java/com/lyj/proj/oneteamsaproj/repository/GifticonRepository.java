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


}
