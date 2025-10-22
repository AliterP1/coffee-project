package com.example.mycoffeedemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class CoffeeContentRequestDTO {
    private String title;
    private String content;
    private List<String> images;
    private Long authorId;
}
