package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ServiceProductMapper {

    @Select("""
            <script>
            SELECT * FROM service_product
            WHERE deleted = 0
              AND audit_status = 1
            <if test='categoryId != null'>
              AND category_id = #{categoryId}
            </if>
            <if test='tradeType != null'>
              AND trade_type = #{tradeType}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='status == null and tradeType == 1'>
              AND status IN (1, 3)
            </if>
            <if test='status == null and tradeType == 2'>
              AND status IN (0, 1, 3)
            </if>
            <if test='status == null and tradeType == null'>
              AND ((trade_type = 1 AND status IN (1, 3)) OR (trade_type = 2 AND status IN (0, 1, 3)))
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
        List<ServiceProduct> selectPage(@Param("categoryId") Long categoryId,
          @Param("tradeType") Integer tradeType,
          @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_product
            WHERE deleted = 0
              AND audit_status = 1
            <if test='categoryId != null'>
              AND category_id = #{categoryId}
            </if>
            <if test='tradeType != null'>
              AND trade_type = #{tradeType}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='status == null and tradeType == 1'>
              AND status IN (1, 3)
            </if>
            <if test='status == null and tradeType == 2'>
              AND status IN (0, 1, 3)
            </if>
            <if test='status == null and tradeType == null'>
              AND ((trade_type = 1 AND status IN (1, 3)) OR (trade_type = 2 AND status IN (0, 1, 3)))
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countPage(@Param("categoryId") Long categoryId,
            @Param("tradeType") Integer tradeType,
            @Param("status") Integer status,
            @Param("keyword") String keyword);

    @Select("SELECT * FROM service_product WHERE id = #{id} AND deleted = 0")
    ServiceProduct selectById(@Param("id") Long id);

    @Insert("""
            INSERT INTO service_product(user_id, post_id, title, description, images, category_id, original_price, price,
              is_negotiable, trade_type, trade_location, product_condition, view_count, want_count, audit_status, status,
              create_time, update_time, deleted)
            VALUES(#{userId}, #{postId}, #{title}, #{description}, #{images}, #{categoryId}, #{originalPrice}, #{price},
              #{isNegotiable}, #{tradeType}, #{tradeLocation}, #{productCondition}, 0, 0, #{auditStatus}, #{status},
              NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceProduct product);

    @Update("""
            UPDATE service_product
            SET title = #{title},
                description = #{description},
                images = #{images},
                category_id = #{categoryId},
                original_price = #{originalPrice},
                price = #{price},
                is_negotiable = #{isNegotiable},
                trade_type = #{tradeType},
                trade_location = #{tradeLocation},
                product_condition = #{productCondition},
                status = #{status},
                update_time = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int updateByOwner(ServiceProduct product);

    @Update("UPDATE service_product SET deleted = 1, update_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND deleted = 0")
    int deleteByOwner(@Param("id") Long id, @Param("userId") Long userId);

    /** 管理员删除（不限制 owner） */
    @Update("UPDATE service_product SET deleted = 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int deleteById(@Param("id") Long id);

    @Insert("INSERT IGNORE INTO forum_favorite(user_id, target_type, target_id, create_time) VALUES(#{userId}, 2, #{productId}, NOW())")
    int insertWant(@Param("productId") Long productId, @Param("userId") Long userId);

    @Update("UPDATE service_product SET want_count = want_count + #{delta}, update_time = NOW() WHERE id = #{productId} AND deleted = 0")
    int updateWantCount(@Param("productId") Long productId, @Param("delta") Integer delta);

    @Update("UPDATE service_product SET post_id = #{postId}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updatePostId(@Param("id") Long id, @Param("postId") Long postId);

    @Select("""
            SELECT * FROM service_product
            WHERE user_id = #{userId}
              AND deleted = 0
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<ServiceProduct> selectMyProducts(@Param("userId") Long userId,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("SELECT COUNT(1) FROM service_product WHERE user_id = #{userId} AND deleted = 0")
    Long countMyProducts(@Param("userId") Long userId);

    @Update("UPDATE service_product SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int increaseViewCount(@Param("id") Long id);

    @Select("""
            <script>
            SELECT * FROM service_product
            WHERE deleted = 0
            <if test='categoryId != null'>
              AND category_id = #{categoryId}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='auditStatus != null'>
              AND audit_status = #{auditStatus}
            </if>
            <if test='tradeType != null'>
              AND trade_type = #{tradeType}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceProduct> selectAdminPage(@Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
          @Param("tradeType") Integer tradeType,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_product
            WHERE deleted = 0
            <if test='categoryId != null'>
              AND category_id = #{categoryId}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='auditStatus != null'>
              AND audit_status = #{auditStatus}
            </if>
            <if test='tradeType != null'>
              AND trade_type = #{tradeType}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countAdminPage(@Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
          @Param("tradeType") Integer tradeType,
            @Param("keyword") String keyword);

    @Update("UPDATE service_product SET audit_status = #{auditStatus}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);

    @Update("UPDATE service_product SET status = #{status}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE service_product SET status = #{toStatus}, update_time = NOW() WHERE id = #{id} AND status = #{fromStatus} AND deleted = 0")
    int updateStatusWithExpected(@Param("id") Long id,
                   @Param("fromStatus") Integer fromStatus,
                   @Param("toStatus") Integer toStatus);

    @Select("SELECT COUNT(1) FROM service_product WHERE deleted = 0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM service_product WHERE deleted = 0 AND audit_status = #{auditStatus}")
    Long countByAuditStatus(@Param("auditStatus") Integer auditStatus);
}
