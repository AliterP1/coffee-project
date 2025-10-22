package com.example.mycoffeedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mycoffeedemo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
