package com.example.mycoffeedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mycoffeedemo.entity.CoffeeContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoffeeContentMapper extends BaseMapper<CoffeeContent> {
}
