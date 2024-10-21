package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BoardRepository {

    @Select("""
			SELECT *
			FROM board
			WHERE id = #{boardId}
			AND delStatus = 0
				""")
    public Board getBoardById(int boardId);

}