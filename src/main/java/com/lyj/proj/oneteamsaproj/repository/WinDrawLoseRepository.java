package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WinDrawLoseRepository {

    // 승/무/패 예측 DB에 저장
    @Insert("INSERT INTO winDrawLose (gameId, memberId, prediction) " +
            "VALUES (#{gameId}, #{memberId}, #{prediction})")
    void insertPrediction(WinDrawLose winDrawLose);

    // 회원의 해당 게임에 대한 승/무/패 예측 정보 가져오기
    @Select("SELECT * FROM winDrawLose WHERE gameId = #{gameId} AND memberId = #{memberId}")
    WinDrawLose findByGameIdAndMemberId(@Param("gameId") int gameId, @Param("memberId") int memberId);

    // 회원의 모든 승/무/패 예측 정보 가져오기
    @Select("SELECT * FROM winDrawLose WHERE memberId = #{memberId}")
    List<WinDrawLose> findAllByMemberId(@Param("memberId") int memberId);

    // 특정 경기의 모든 예측 정보 가져오기
    @Select("SELECT * FROM winDrawLose WHERE gameId = #{gameId}")
    List<WinDrawLose> findByGameId(@Param("gameId") int gameId);
}

