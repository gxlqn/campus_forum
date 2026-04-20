package com.campus.forum.mapper;

import com.campus.forum.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND deleted = 0")
    SysUser selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_user WHERE openid = #{openid} AND deleted = 0")
    SysUser selectByOpenid(@Param("openid") String openid);

    @Update("UPDATE sys_user SET openid = #{openid}, union_id = #{unionId}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateOpenidAndUnionid(@Param("id") Long id,
                               @Param("openid") String openid,
                               @Param("unionId") String unionId);

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE student_id = #{studentId} AND deleted = 0")
    SysUser selectByStudentId(@Param("studentId") String studentId);

    @Select("SELECT * FROM sys_user WHERE union_id = #{unionId} AND deleted = 0")
    SysUser selectByUnionId(@Param("unionId") String unionId);

    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectUserRoles(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO sys_user(openid, union_id, student_id, username, password, nickname, avatar, gender, phone,
              email, bio, college, major, grade, user_type, status, is_verified, create_time, update_time, deleted)
            VALUES(#{openid}, #{unionId}, #{studentId}, #{username}, #{password}, #{nickname}, #{avatar}, #{gender},
              #{phone}, #{email}, #{bio}, #{college}, #{major}, #{grade}, #{userType}, #{status}, #{isVerified},
              NOW(), NOW(), 0)
            """)
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("""
            UPDATE sys_user
            SET student_id = #{studentId},
                user_type = #{userType},
                is_verified = 1,
                update_time = NOW()
            WHERE id = #{userId} AND deleted = 0
            """)
    int bindStudentId(@Param("userId") Long userId,
            @Param("studentId") String studentId,
            @Param("userType") Integer userType);

    @Update("""
            UPDATE sys_user
            SET nickname = #{nickname},
                avatar = #{avatar},
                gender = #{gender},
                phone = #{phone},
                email = #{email},
                bio = #{bio},
                college = #{college},
                major = #{major},
                grade = #{grade},
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateUserInfo(SysUser user);

    @Update("UPDATE sys_user SET last_login_time = NOW(), last_login_ip = #{ip}, update_time = NOW() WHERE id = #{userId}")
    int updateLastLogin(@Param("userId") Long userId, @Param("ip") String ip);

    @Select("SELECT COUNT(1) FROM sys_user WHERE deleted = 0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM sys_user WHERE deleted = 0 AND last_login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)")
    Long countActiveUsers();

    @Select("SELECT COUNT(1) FROM sys_user WHERE deleted = 0 AND DATE(create_time) = CURRENT_DATE")
    Long countNewUsersToday();

    @Select("SELECT id, nickname, avatar, student_id, user_type, status, is_verified, create_time " +
            "FROM sys_user WHERE deleted = 0 ORDER BY id DESC LIMIT 50")
    List<java.util.Map<String, Object>> selectDevUserList();

    @Update("UPDATE sys_user SET balance = balance + #{amount} WHERE id = #{id} AND balance + #{amount} >= 0")
    int updateBalance(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);

    @Update("UPDATE sys_user SET credit_score = credit_score + #{scoreDelta} WHERE id = #{id}")
    int updateCreditScore(@Param("id") Long id, @Param("scoreDelta") Integer scoreDelta);
}
