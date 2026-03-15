package com.example.mycoffeedemo.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mycoffeedemo.dto.ProductResponseDTO;
import com.example.mycoffeedemo.entity.Product;
import com.example.mycoffeedemo.service.ProductService;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<IPage<ProductResponseDTO>> getProductPage(@RequestParam(defaultValue = "1") Long page,
                                                            @RequestParam(defaultValue = "10") Long size){
        return productService.getProductPage(page,size);
    }


    // 商家查询商品
    @GetMapping("/userId")
    public Result<IPage<ProductResponseDTO>> getUserIdProductPage(@RequestParam(defaultValue = "1") Long page,
                                                                  @RequestParam(defaultValue = "10") Long size,
                                                                  @RequestParam String userId){
        return productService.getUserIdProductPage(page,size,userId);
    }
    //新增商品
    @PostMapping
    public Result<ProductResponseDTO> addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }
    //商品更新
    @PutMapping("/{id}")
    public Result<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id,product);
    }
    //商品删除
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
    //添加商品图片
    @PostMapping("/{id}/images")
    public Result<List<String>> uploadProductImages(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) throws JsonProcessingException {
        return productService.uploadProductImages(id,files);
    }
}
