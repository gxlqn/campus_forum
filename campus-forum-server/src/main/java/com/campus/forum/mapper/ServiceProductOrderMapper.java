package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceProductOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ServiceProductOrderMapper {

    @Insert("""
                        INSERT INTO service_product_order(order_no, product_id, buyer_id, seller_id, amount, status,
                            meetup_place, meetup_time, meetup_code, meetup_verified, reschedule_count,
                            create_time, update_time, deleted)
                        VALUES(#{orderNo}, #{productId}, #{buyerId}, #{sellerId}, #{amount}, #{status},
                            #{meetupPlace}, #{meetupTime}, #{meetupCode}, #{meetupVerified}, #{rescheduleCount},
                            NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceProductOrder order);

    @Select("SELECT * FROM service_product_order WHERE id = #{id} AND deleted = 0")
    ServiceProductOrder selectById(@Param("id") Long id);

    @Select("SELECT * FROM service_product_order WHERE order_no = #{orderNo} AND deleted = 0")
    ServiceProductOrder selectByOrderNo(@Param("orderNo") String orderNo);

        @Select("""
                        SELECT * FROM service_product_order
                        WHERE product_id = #{productId}
                            AND buyer_id = #{buyerId}
                            AND status IN (0, 1)
                            AND deleted = 0
                        ORDER BY create_time DESC
                        LIMIT 1
                        """)
        ServiceProductOrder selectActiveByProductAndBuyer(@Param("productId") Long productId,
                                                                                                            @Param("buyerId") Long buyerId);

    @Update("""
            UPDATE service_product_order
            SET status = 2,
                cancel_reason = #{cancelReason},
                cancel_time = NOW(),
                update_time = NOW()
            WHERE id = #{id} AND status IN (0, 1) AND deleted = 0
            """)
    int cancelOrder(@Param("id") Long id, @Param("cancelReason") String cancelReason);

        @Update("""
            UPDATE service_product_order
            SET status = 1,
            update_time = NOW()
            WHERE id = #{id} AND status = 0 AND deleted = 0
            """)
        int acceptPendingOrder(@Param("id") Long id);

        @Update("""
            UPDATE service_product_order
            SET status = 4,
            cancel_reason = #{reason},
            update_time = NOW()
            WHERE id = #{id} AND status = 0 AND deleted = 0
            """)
        int rejectPendingOrder(@Param("id") Long id, @Param("reason") String reason);

        @Update("""
            UPDATE service_product_order
            SET status = 5,
            cancel_reason = '未被卖家选中',
            update_time = NOW()
            WHERE product_id = #{productId}
              AND id != #{acceptedOrderId}
              AND status = 0
              AND deleted = 0
            """)
        int rejectOtherPendingByProduct(@Param("productId") Long productId,
                        @Param("acceptedOrderId") Long acceptedOrderId);

    @Update("""
            UPDATE service_product_order
            SET status = 3,
                finish_time = NOW(),
                update_time = NOW()
            WHERE id = #{id} AND status = 1 AND deleted = 0
            """)
    int completeOrder(@Param("id") Long id);

    @Update("""
            UPDATE service_product_order
            SET meetup_place = #{meetupPlace},
                meetup_time = #{meetupTime},
                update_time = NOW()
            WHERE id = #{id} AND status = 1 AND deleted = 0
            """)
    int updateMeetup(@Param("id") Long id,
                     @Param("meetupPlace") String meetupPlace,
                     @Param("meetupTime") java.time.LocalDateTime meetupTime);

    @Update("""
            UPDATE service_product_order
            SET meetup_place = #{meetupPlace},
                meetup_time = #{meetupTime},
                reschedule_count = reschedule_count + 1,
                update_time = NOW()
            WHERE id = #{id} AND status = 1 AND deleted = 0
              AND (reschedule_count IS NULL OR reschedule_count < 3)
            """)
    int rescheduleMeetup(@Param("id") Long id,
                         @Param("meetupPlace") String meetupPlace,
                         @Param("meetupTime") java.time.LocalDateTime meetupTime);

    @Update("""
            UPDATE service_product_order
            SET meetup_verified = 1,
                update_time = NOW()
            WHERE id = #{id} AND status = 1 AND deleted = 0
              AND meetup_code = #{meetupCode}
            """)
    int verifyMeetupCode(@Param("id") Long id,
                         @Param("meetupCode") String meetupCode);

    @Select("""
            <script>
            SELECT * FROM service_product_order
            WHERE deleted = 0
            <if test='userId != null'>
              AND (
                <if test='role == null or role == ""'>
                    buyer_id = #{userId} OR seller_id = #{userId}
                </if>
                <if test='role == "buyer"'>
                    buyer_id = #{userId}
                </if>
                <if test='role == "seller"'>
                    seller_id = #{userId}
                </if>
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceProductOrder> selectMyOrders(@Param("userId") Long userId,
                                             @Param("role") String role,
                                             @Param("offset") Long offset,
                                             @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_product_order
            WHERE deleted = 0
            <if test='userId != null'>
              AND (
                <if test='role == null or role == ""'>
                    buyer_id = #{userId} OR seller_id = #{userId}
                </if>
                <if test='role == "buyer"'>
                    buyer_id = #{userId}
                </if>
                <if test='role == "seller"'>
                    seller_id = #{userId}
                </if>
              )
            </if>
            </script>
            """)
    Long countMyOrders(@Param("userId") Long userId, @Param("role") String role);
}
