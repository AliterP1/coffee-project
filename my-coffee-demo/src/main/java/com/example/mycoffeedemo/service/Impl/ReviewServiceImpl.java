package com.example.mycoffeedemo.service.Impl;

import com.alipay.easysdk.kernel.util.JsonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.ReviewRequestDTO;
import com.example.mycoffeedemo.dto.ReviewResponseDTO;
import com.example.mycoffeedemo.entity.*;
import com.example.mycoffeedemo.mapper.OrderItemMapper;
import com.example.mycoffeedemo.mapper.OrderMapper;
import com.example.mycoffeedemo.mapper.ReviewMapper;
import com.example.mycoffeedemo.mapper.UserMapper;
import com.example.mycoffeedemo.service.FileService;
import com.example.mycoffeedemo.service.ReviewService;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper,Review> implements ReviewService {

    @Autowired
    private FileService fileService;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private UserMapper userMapper;


    @Override
    public Result<List<String>> uploadReviewImages(Long id, List<MultipartFile> files) throws JsonProcessingException {
        ArrayList<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            Result<String> uploadResult = fileService.upload(file, "review");
            if (uploadResult.getCode() == 200) {
                urls.add(uploadResult.getData());
            }else  {
                return Result.fail("上传失败");
            }
        }
        Review review = reviewMapper.selectById(id);
        review.setImages(urls);
        reviewMapper.insert(review);
        return Result.success("图片上传成功",urls);
    }

    @Override
    public Result<ReviewResponseDTO> addReview(ReviewRequestDTO dto) throws JsonProcessingException {
        // 1. 校验基本字段
        if (dto.getProductId() == null || dto.getProductId() == 0) {
            return Result.fail("商品ID不能为空");
        }
        if (dto.getUserId() == null || dto.getUserId() == 0) {
            return Result.fail("用户ID不能为空");
        }
        // 2. 根据用户ID和商品ID查询订单

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getProductId, dto.getProductId())
                .inSql(OrderItem::getOrderId, "SELECT id FROM orders WHERE user_id = " + dto.getUserId() + " AND status = 'completed'")
                .last("LIMIT 1");

        OrderItem orderItem = orderItemMapper.selectOne(wrapper);
        if (orderItem == null) {
            return Result.fail("未找到已完成的订单，无法评价");
        }

        Long orderId = orderItem.getOrderId();

        // 3. 检查是否已经评论过
        QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
        reviewWrapper.eq("user_id", dto.getUserId())
                .eq("product_id", dto.getProductId())
                .eq("order_id", orderId);

        Review existingReview = reviewMapper.selectOne(reviewWrapper);
        if (existingReview != null) {
            return Result.fail("您已对该订单商品进行过评论");
        }

        // 4. 保存评论
        Review review = new Review();
        BeanUtils.copyProperties(dto,review);
        review.setOrderId(orderId);
        reviewMapper.insert(review);
        return Result.success("评论提交成功",null);
    }

    @Override
    public Result<IPage<ReviewResponseDTO>> getReview(Long page, Long size, Long productId) {
        Page<Review> pageParam = new Page<>(page, size);
        IPage<Review> reviewPage = reviewMapper.selectPage(pageParam, new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId));

        IPage<ReviewResponseDTO> dtoPage=reviewPage.convert(review -> {
            ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
            BeanUtils.copyProperties(review,reviewResponseDTO);

            if (review.getImages() != null) {
                reviewResponseDTO.setImages(review.getImages());
            }
            if (review.getUserId() != null) {
                User reviewer = userMapper.selectById(review.getUserId());
                if (reviewer != null) {
                    reviewResponseDTO.setUsername(reviewer.getUsername());
                    reviewResponseDTO.setAvatarUrl(reviewer.getAvatarUrl());
                }
            }
           return reviewResponseDTO;
        });
        return Result.success("评论获取成功",dtoPage);
    }
}
