package com.example.mycoffeedemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReviewRequestDTO {
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String comment;
    private List<String> images;
}
