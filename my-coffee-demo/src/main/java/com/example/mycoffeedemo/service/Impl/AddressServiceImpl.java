package com.example.mycoffeedemo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.entity.Address;
import com.example.mycoffeedemo.mapper.AddressMapper;
import com.example.mycoffeedemo.service.AddressService;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public Result<List<Address>> getUserAddresses(Long userId) {
        LambdaQueryWrapper<Address> wapper = new LambdaQueryWrapper<>();
        wapper.eq(Address::getUserId,userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdatedAt);
        List<Address> addresses = addressMapper.selectList(wapper);
        return Result.success("获取地址列表成功", addresses);
    }

    @Override
    public Result<String> addAddress(Address  address) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, address.getUserId());
        Long count = addressMapper.selectCount(wrapper);
        if (count == 0) {
            address.setIsDefault(true);
        }
        addressMapper.insert(address);
        return Result.success("添加地址成功", null);
    }

    @Override
    public Result<Address> updateAddress(Address address) {
        Address existingAddress = addressMapper.selectById(address.getId());
        if (existingAddress == null) {
            return Result.fail("地址不存在");
        }
        if (!existingAddress.getUserId().equals(address.getUserId())) {
            return Result.fail("无权修改此地址");
        }
        addressMapper.updateById(address);
        return  Result.success("更新地址成功", address);
    }

    @Override
    public Result<String> deleteAddress(Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            return Result.fail("地址不存在");
        }

        addressMapper.deleteById(id);
        // 如果删除的是默认地址，则将用户的第一个地址设为默认地址
        if (address.getIsDefault()) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, address.getUserId())
                    .orderByDesc(Address::getUpdatedAt)
                    .last("LIMIT 1");
            Address firstAddress = addressMapper.selectOne(wrapper);
            if (firstAddress != null) {
                firstAddress.setIsDefault(true);
                addressMapper.updateById(firstAddress);
            }
        }
        return Result.success("用户删除成功", null);

    }

    @Override
    @Transactional
    public Result<Address> setDefaultAddress(Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            return Result.fail("地址不存在");
        }
        // 将用户的所有地址设为非默认
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, address.getUserId())
                .eq(Address::getIsDefault, true);
        List<Address> defaultAddresses = addressMapper.selectList(wrapper);
        for (Address defaultAddress : defaultAddresses) {
            defaultAddress.setIsDefault(false);
            addressMapper.updateById(defaultAddress);
        }
        // 设置新的默认地址
        address.setIsDefault(true);
        addressMapper.updateById(address);
        return Result.success("设置默认地址成功",address);
    }
}
