package com.example.mycoffeedemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mycoffeedemo.dto.ProductResponseDTO;
import com.example.mycoffeedemo.entity.Product;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ProductService extends IService<Product> {
    Result<IPage<ProductResponseDTO>> getProductPage(Long page, Long size);

    Result<IPage<ProductResponseDTO>> getUserIdProductPage(Long page, Long size, String userId);

    Result<ProductResponseDTO> addProduct(Product product);

    Result<ProductResponseDTO> updateProduct(Long id, Product product);

    Result<String> deleteProduct(Long id);

    Result<List<String>> uploadProductImages(Long id, List<MultipartFile> files) throws JsonProcessingException;
}
