package com.example.mycoffeedemo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private List<String> images;    // 前端传 List
    private String category;
}

