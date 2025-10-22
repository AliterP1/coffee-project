package com.example.mycoffeedemo.controller;


import com.example.mycoffeedemo.annotation.RequireOwner;
import com.example.mycoffeedemo.entity.Address;
import com.example.mycoffeedemo.service.AddressService;
import com.example.mycoffeedemo.utils.CurrentUserUtil;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;


    /**
     * 获取用户的所有地址
     */
    @GetMapping("/{userId}")
    public Result<List<Address>> getUserAddresses(@PathVariable Long userId) {
        return addressService.getUserAddresses(userId);
    }

    /**
     * 添加新地址
     */
    @PostMapping("/add")
    public Result<String> addAddress(@RequestBody Address address) {
        return addressService.addAddress(address);
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    public Result<Address> updateAddress(@RequestBody Address address) {
        return addressService.updateAddress(address);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("'/delete/{id}")
    @RequireOwner(resource = "address", idArg = "id")
    public Result<String> deleteAddress(@PathVariable Long id) {
        return addressService.deleteAddress(id);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{id}/default")
    @RequireOwner(resource = "address", idArg = "id")
    public Result<Address> setDefaultAddress(@PathVariable Long id) {
        return addressService.setDefaultAddress(id);
    }
}
