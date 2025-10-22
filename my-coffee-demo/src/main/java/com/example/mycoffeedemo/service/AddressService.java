package com.example.mycoffeedemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mycoffeedemo.entity.Address;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.stereotype.Service;

import java.util.List;



public interface AddressService extends IService<Address> {
    Result<List<Address>> getUserAddresses(Long userId);

    Result<String> addAddress(Address address);

    Result<Address> updateAddress(Address address);

    Result<String> deleteAddress(Long id);

    Result<Address> setDefaultAddress(Long id);
}
