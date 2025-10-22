package com.example.mycoffeedemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReviewResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String comment;
    private List<String> images;
    private String avatarUrl;
}

