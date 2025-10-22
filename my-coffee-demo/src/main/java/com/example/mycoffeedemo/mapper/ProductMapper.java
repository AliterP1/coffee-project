package com.example.mycoffeedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mycoffeedemo.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
