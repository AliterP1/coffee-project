package com.example.mycoffeedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mycoffeedemo.entity.MerchantOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantOrderMapper extends BaseMapper<MerchantOrder> {
}
