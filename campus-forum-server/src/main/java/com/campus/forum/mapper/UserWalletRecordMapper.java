package com.campus.forum.mapper;

import com.campus.forum.entity.UserWalletRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserWalletRecordMapper {
    @Insert("""
            INSERT INTO user_wallet_record(user_id, amount, type, relation_id, create_time)
            VALUES(#{userId}, #{amount}, #{type}, #{relationId}, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserWalletRecord record);
}
