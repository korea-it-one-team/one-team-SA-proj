package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WinDrawLoseRepository {

    @Insert("INSERT INTO winDrawLose (gameId, memberId, prediction) " +
            "VALUES (#{gameId}, #{memberId}, #{prediction})")
    void insertPrediction(WinDrawLose winDrawLose);

    @Select("SELECT * FROM winDrawLose WHERE gameId = #{gameId} AND memberId = #{memberId}")
    WinDrawLose findByGameIdAndMemberId(@Param("gameId") int gameId, @Param("memberId") int memberId);

    @Select("SELECT * FROM winDrawLose WHERE memberId = #{memberId}")
    List<WinDrawLose> findAllByMemberId(@Param("memberId") int memberId);
}
