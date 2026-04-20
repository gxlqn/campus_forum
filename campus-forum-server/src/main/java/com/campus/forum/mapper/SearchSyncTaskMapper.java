package com.campus.forum.mapper;

import com.campus.forum.entity.SearchSyncTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SearchSyncTaskMapper {

    @Insert("""
            INSERT INTO search_sync_task(index_name, entity_type, document_id, operation_type, payload_json,
                                         status, retry_count, max_retry, last_error, next_retry_time, last_tried_time,
                                         create_time, update_time)
            VALUES(#{indexName}, #{entityType}, #{documentId}, #{operationType}, #{payloadJson},
                   #{status}, #{retryCount}, #{maxRetry}, #{lastError}, #{nextRetryTime}, #{lastTriedTime},
                   NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SearchSyncTask task);

    @Select("""
            SELECT *
            FROM search_sync_task
            WHERE status = 0
              AND next_retry_time <= NOW()
            ORDER BY id ASC
            LIMIT #{limit}
            """)
    List<SearchSyncTask> selectDuePendingTasks(@Param("limit") Integer limit);

    @Update("""
            UPDATE search_sync_task
            SET status = 3,
                last_tried_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
              AND status = 0
            """)
    int markProcessing(@Param("id") Long id);

    @Update("""
            UPDATE search_sync_task
            SET status = 1,
                last_error = NULL,
                update_time = NOW()
            WHERE id = #{id}
            """)
    int markSuccess(@Param("id") Long id);

    @Update("""
            UPDATE search_sync_task
            SET status = #{status},
                retry_count = #{retryCount},
                last_error = #{lastError},
                next_retry_time = #{nextRetryTime},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int markFailed(@Param("id") Long id,
                   @Param("status") Integer status,
                   @Param("retryCount") Integer retryCount,
                   @Param("lastError") String lastError,
                   @Param("nextRetryTime") LocalDateTime nextRetryTime);

    @Select("""
            <script>
            SELECT *
            FROM search_sync_task
            <where>
              <if test='status != null'>
                status = #{status}
              </if>
              <if test='entityType != null and entityType != ""'>
                AND entity_type = #{entityType}
              </if>
            </where>
            ORDER BY id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<SearchSyncTask> selectPage(@Param("status") Integer status,
                                    @Param("entityType") String entityType,
                                    @Param("offset") Long offset,
                                    @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM search_sync_task
            <where>
              <if test='status != null'>
                status = #{status}
              </if>
              <if test='entityType != null and entityType != ""'>
                AND entity_type = #{entityType}
              </if>
            </where>
            </script>
            """)
    Long countPage(@Param("status") Integer status,
                   @Param("entityType") String entityType);

    @Update("""
            UPDATE search_sync_task
            SET status = 0,
                next_retry_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
              AND status IN (2, 3)
            """)
    int retryNow(@Param("id") Long id);
}
