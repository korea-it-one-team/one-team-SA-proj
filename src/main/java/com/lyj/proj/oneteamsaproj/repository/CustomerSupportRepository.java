package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomerSupportRepository {


    @Select("""
            SELECT F.id, C.category_name, F.question, F.answer FROM faq AS F
            INNER JOIN support_Category AS C
            ON F.category_id = C.id;
            """)
    public List<Faq> getFaqs();

    @Select("SELECT * FROM support_Category")
    List<faq_Categorys> getFaqs_Category();

    @Insert("""
            INSERT INTO consultations
            SET member_id = #{memberId},
            title = #{title},
            content = #{content}
            """)
    void addConsultation(String title, String content, int memberId);

    @Select("SELECT * FROM consultations WHERE id = LAST_INSERT_ID()")
    Consultation lastConsultation();


    @Select("SELECT * FROM consultations")
    List<Consultation> allFind();

    @Select("SELECT * FROM consultations WHERE member_id = #{memberId}")
    List<Consultation> getHistory(int memberId);
}
