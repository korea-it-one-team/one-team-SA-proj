package com.lyj.proj.oneteamsaproj.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.lyj.proj.oneteamsaproj.vo.Member;

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
	public void modify(int loginedMemberId, String loginPw, String name, String cellphoneNum,
			String email);

	@Update("""
			<script>
				UPDATE `member`
				<set>
					<if test="name != null">
						name = #{name},
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
	public void modifyWithoutPw(int loginedMemberId, String name, String cellphoneNum, String email);

}