package com.example.mycoffeedemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class CoffeeContentResponseDTO {
    private Long id;
    private String title;
    private String content;
    private List<String> images;
    private Long authorId;
    private String authorName;
}

