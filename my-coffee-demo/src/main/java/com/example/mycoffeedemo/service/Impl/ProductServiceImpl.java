package com.example.mycoffeedemo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.ProductResponseDTO;
import com.example.mycoffeedemo.entity.Product;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.mapper.ProductMapper;
import com.example.mycoffeedemo.mapper.UserMapper;
import com.example.mycoffeedemo.utils.FileService;
import com.example.mycoffeedemo.service.ProductService;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>implements ProductService {


    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileService fileService;
    @Override
    public Result<IPage<ProductResponseDTO>> getProductPage(Long page, Long size) {
        Page<Product> pageParam = new Page<>(page, size);
        IPage<Product> productPage = productMapper.selectPage(pageParam, new LambdaQueryWrapper<Product>()
                .ne(Product::getIsActive,0));

        IPage<ProductResponseDTO> dtoPage = productPage.convert(product -> {
            ProductResponseDTO dto = new ProductResponseDTO();
            BeanUtils.copyProperties(product, dto);
            if (product.getImages() != null) {
                dto.setImages(product.getImages());
                }
            return dto;
        });
        return Result.success("分页查询所有商品成功",dtoPage);
    }

    @Override
    public Result<IPage<ProductResponseDTO>> getUserIdProductPage(Long page, Long size, String userId) {
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> userProduct = productMapper.selectPage(productPage, new QueryWrapper<Product>().eq("merchant_id", userId));
        IPage<ProductResponseDTO> dtoPage=userProduct.convert(product -> {
            ProductResponseDTO dto = new ProductResponseDTO();
            BeanUtils.copyProperties(product, dto);
            if (product.getImages() != null) {
                dto.setImages(product.getImages());
            }
            return dto;
        });
        return Result.success("分页查询成功",dtoPage);
    }

    @Override
    public Result<ProductResponseDTO> addProduct(Product product) {
        Long merchantId = product.getMerchantId();

        if (merchantId == null) {
            return Result.fail("缺少商家ID，无法添加商品");
        }

        //检查用户是否存在且是商家
        User user = userMapper.selectById(merchantId);
        if (user == null) {
            return Result.fail("商家不存在");
        }

        if (!"merchant".equalsIgnoreCase(user.getRole())) {
            return Result.fail("该用户不是商家，无权添加商品");
        }

        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId)
                .eq("name", product.getName());
        Product existingProduct = productMapper.selectOne(wrapper);

        if (existingProduct != null) {
            return Result.fail("该商品名称已存在，请勿重复添加");
        }

        int insert = productMapper.insert(product);
        if (insert > 0) {
            ProductResponseDTO dto = new ProductResponseDTO();
            BeanUtils.copyProperties(product, dto);
            return Result.success("商品新增成功", dto);
        }
        return Result.fail("商品新增失败");
    }

    @Override
    public Result<ProductResponseDTO> updateProduct(Long id, Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            return Result.fail("未找到该商品");
        }
        product.setId(id);
        int updated = productMapper.updateById(product);

        if (updated > 0) {
            ProductResponseDTO dto = new ProductResponseDTO();
            BeanUtils.copyProperties(product, dto);
            return Result.success("商品更新成功", dto);
        }
        return Result.fail("商品更新失败");
    }

    @Override
    public Result<String> deleteProduct(Long id) {
        int deleted = productMapper.deleteById(id);
        if (deleted > 0) {
            return Result.success("商品删除成功");
        }
        return Result.fail("商品删除失败或商品不存在");
    }

    @Override
    public Result<List<String>> uploadProductImages(Long id, List<MultipartFile> files) throws JsonProcessingException {
        ArrayList<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            Result<String> uploadResult = fileService.upload(file, "products");
            if (uploadResult.getCode() == 200) {
                urls.add(uploadResult.getData());
            }else {
                Result.fail("文件上传失败 ");
            }
        }
        Product product = productMapper.selectById(id);
        product.setImages(urls);
        productMapper.updateById(product);
        return Result.success("图片上传成功",urls);
    }
}
