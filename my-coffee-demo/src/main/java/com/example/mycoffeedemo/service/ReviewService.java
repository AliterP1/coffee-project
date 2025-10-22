package com.example.mycoffeedemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mycoffeedemo.dto.ReviewRequestDTO;
import com.example.mycoffeedemo.dto.ReviewResponseDTO;
import com.example.mycoffeedemo.entity.Review;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ReviewService extends IService<Review>{

    Result<List<String>> uploadReviewImages(Long id, List<MultipartFile> files) throws JsonProcessingException;

    Result<ReviewResponseDTO> addReview(ReviewRequestDTO reviewRequestDTO) throws JsonProcessingException;

    Result<IPage<ReviewResponseDTO>> getReview(Long page, Long size, Long productId);
}
