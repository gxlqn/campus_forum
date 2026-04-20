package com.campus.forum.mapper;

import com.campus.forum.entity.SysPlatformWallet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysPlatformWalletMapper {
    int insert(SysPlatformWallet record);
}
