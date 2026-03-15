package com.example.mycoffeedemo.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mycoffeedemo.dto.ReviewRequestDTO;
import com.example.mycoffeedemo.dto.ReviewResponseDTO;
import com.example.mycoffeedemo.service.ReviewService;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/{id}/images")
    public Result<List<String>> uploadReviewImages(@PathVariable Long id,
                                             @RequestParam("files") List<MultipartFile> files) throws JsonProcessingException {
        return reviewService.uploadReviewImages(id,files);
    }

    @PostMapping("/addReview")
    public Result<ReviewResponseDTO>  addReview(@RequestBody ReviewRequestDTO reviewRequestDTO) throws JsonProcessingException {
        return reviewService.addReview(reviewRequestDTO);
    }

    @GetMapping("/product")
    public Result<IPage<ReviewResponseDTO>> getReview(@RequestParam(defaultValue = "1") Long page,
                                                      @RequestParam(defaultValue = "10") Long size,
                                                      @RequestParam Long productId) {
        return reviewService.getReview(page, size, productId);
    }
}
