package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WinDrawLoseRepository {

    @Insert("INSERT INTO prediction (gameId, memberId, prediction) VALUES (#{gameId}, #{memberId}, #{prediction})")
    void save(WinDrawLose prediction);

    @Select("SELECT * FROM gameSchedule WHERE id = #{gameId}")
    GameSchedule findGameById(int gameId);

    @Select("SELECT * FROM prediction WHERE gameId = #{gameId} AND memberId = #{memberId}")
    WinDrawLose findPredictionByGameIdAndMemberId(@Param("gameId") int gameId, @Param("memberId") int memberId);
}
