package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.Member;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberRepository {

    @Select("SELECT LAST_INSERT_ID();")
    public int getLastInsertId();

    @Insert("""
            INSERT INTO `member` 
            SET regDate = NOW(), 
            updateDate = NOW(), 
            loginId = #{loginId}, 
            loginPw = #{loginPw}, 
            `name` = #{name}, 
            nickname = #{nickname}, 
            cellphoneNum = #{cellphoneNum}, 
            email = #{email},
            points = 100
            """)
    public void doJoin(String loginId, String loginPw, String name, String nickname, String cellphoneNum, String email);

    @Insert("""
            INSERT INTO `member` 
            SET regDate = NOW(), 
            updateDate = NOW(), 
            loginId = #{loginId}, 
            loginPw = #{loginPw}, 
            `name` = #{name}, 
            cellphoneNum = #{cellphoneNum}, 
            email = #{email}
            """)
    public void doSign(String loginId, String loginPw, String name, String cellphoneNum, String email);

    @Select("SELECT * FROM `member` WHERE id = #{id}")
    public Member getMemberById(int id);

    @Select("""
            SELECT *
            FROM `member`
            WHERE loginId = #{loginId}
            """)
    public Member getMemberByLoginId(String loginId);

    @Select("""
            SELECT *
            FROM `member`
            WHERE name = #{name}
            AND email = #{email}
            """)
    public Member getMemberByNameAndEmail(String name, String email);

    // 승부 예측 성공 시 포인트 + 5 추가 메서드
    @Update("UPDATE `member` SET points = points + #{points} WHERE id = #{memberId}")
    public void addPoints(int memberId, int points);

    @Update("""
            <script>
                UPDATE `member`
                <set>
                    <if test="loginPw != null">
                        loginPw = #{loginPw},
                    </if>
                    <if test="name != null">
                        name = #{name},
                    </if>
                    <if test="nickname != null">
                        nickname = #{nickname},
                    </if>
                    <if test="cellphoneNum != null">
                        cellphoneNum = #{cellphoneNum},
                    </if>
                    <if test="email != null">
                        email = #{email},
                    </if>
                    updateDate = NOW()
                </set>
                WHERE id = #{loginedMemberId}
            </script>
            """)
    public void modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphoneNum, String email);

    @Update("""
            <script>
                UPDATE `member`
                <set>
                    <if test="name != null">
                        name = #{name},
                    </if>
                    <if test="nickname != null">
                        nickname = #{nickname},
                    </if>
                    <if test="cellphoneNum != null">
                        cellphoneNum = #{cellphoneNum},
                    </if>
                    <if test="email != null">
                        email = #{email},
                    </if>
                    updateDate = NOW()
                </set>
                WHERE id = #{loginedMemberId}
            </script>
            """)
    public void modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphoneNum, String email);

    @Update("""
            UPDATE `member`
            SET updateDate = NOW(),
            points = points - ${point}
            WHERE id = #{loginedMemberId}
            """)
    void modifyPoint(int point, int loginedMemberId);

    @Select("""
            <script>
            SELECT COUNT(*) AS cnt
            FROM `member` AS M
            WHERE 1
            <if test="authLevel != 0">
                AND M.authLevel = #{authLevel}
            </if>
            <if test="searchKeyword != ''">
                <choose>
                    <when test="searchKeywordTypeCode == 'loginId'">
                        AND M.loginId LIKE CONCAT('%', #{searchKeyword}, '%')
                    </when>
                    <when test="searchKeywordTypeCode == 'name'">
                        AND M.name LIKE CONCAT('%', #{searchKeyword}, '%')
                    </when>
                    <when test="searchKeywordTypeCode == 'nickname'">
                        AND M.nickname LIKE CONCAT('%', #{searchKeyword}, '%')
                    </when>
                    <otherwise>
                        AND (
                            M.loginId LIKE CONCAT('%', #{searchKeyword}, '%')
                            OR M.name LIKE CONCAT('%', #{searchKeyword}, '%')
                            OR M.nickname LIKE CONCAT('%', #{searchKeyword}, '%')
                            )
                    </otherwise>
                </choose>
            </if>
            </script>
            """)
    int getMembersCount(String authLevel, String searchKeywordTypeCode, String searchKeyword);

    @Select("""
            <script>
            SELECT M.*
            FROM `member` AS M
            WHERE 1
            <if test="authLevel != 0">
                AND M.authLevel = #{authLevel}
            </if>
            <if test="searchKeyword != ''">
                <choose>
                    <when test="searchKeywordTypeCode == 'loginId'">
                        AND M.loginId LIKE CONCAT('%', #{searchKeyword}, '%')
                    </when>
                    <when test="searchKeywordTypeCode == 'name'">
                        AND M.name LIKE CONCAT('%', #{searchKeyword}, '%')
                    </when>
                    <when test="searchKeywordTypeCode == 'nickname'">
                        AND M.nickname LIKE CONCAT('%', #{searchKeyword}, '%')
                    </when>
                    <otherwise>
                        AND (
                            M.loginId LIKE CONCAT('%', #{searchKeyword}, '%')
                            OR M.name LIKE CONCAT('%', #{searchKeyword}, '%')
                            OR M.nickname LIKE CONCAT('%', #{searchKeyword}, '%')
                            )
                    </otherwise>
                </choose>
            </if>
            ORDER BY M.id DESC
            <if test="limitTake != -1">
                LIMIT #{limitStart}, #{limitTake}
            </if>
            </script>
            """)
    List<Member> getForPrintMembers(String authLevel, String searchKeywordTypeCode, String searchKeyword, int limitStart, int limitTake);

    @Update("""
            <script>
            UPDATE `member`
            <set>
                updateDate = NOW(),
                delStatus = 1,
                delDate = NOW(),
            </set>
            WHERE id = #{id}
            </script>
            """)
    public void deleteMember(int id);
}
