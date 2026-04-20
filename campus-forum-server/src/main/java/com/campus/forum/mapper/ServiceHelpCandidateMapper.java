package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceHelpCandidate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ServiceHelpCandidateMapper {

    @Select("SELECT * FROM service_help_candidate WHERE help_id = #{helpId} AND user_id = #{userId} LIMIT 1")
    ServiceHelpCandidate selectByHelpIdAndUserId(@Param("helpId") Long helpId, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO service_help_candidate(help_id, user_id, credit_score, is_selected, create_time)
            VALUES(#{helpId}, #{userId}, #{creditScore}, 0, NOW())
            """)
    int insertCandidate(@Param("helpId") Long helpId,
                        @Param("userId") Long userId,
                        @Param("creditScore") Integer creditScore);

    @Select("""
            SELECT * FROM service_help_candidate
            WHERE help_id = #{helpId}
            ORDER BY credit_score DESC, create_time ASC, id ASC
            LIMIT 1
            """)
    ServiceHelpCandidate selectBestCandidate(@Param("helpId") Long helpId);

    @Update("UPDATE service_help_candidate SET is_selected = 1 WHERE id = #{candidateId}")
    int markSelected(@Param("candidateId") Long candidateId);
}
