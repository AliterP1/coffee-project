package com.example.mycoffeedemo.dto;

import com.example.mycoffeedemo.entity.CoffeeContent;
import com.example.mycoffeedemo.entity.Product;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.mycoffeedemo.service.Impl.CoffeeContentServiceImpl.parseImageList;

@Data
@Accessors(chain = true)
public class SearchResultDTO {
    /** 通用字段 **/
    private Long id;
    private String title;       // 商品：name；内容：title
    private String content;     // 商品：description；内容：content
    private List<String> images;      // 图片路径或URL
    private LocalDateTime createdAt;
    private String type;        // "PRODUCT" 或 "CONTENT"

    /** 商品特有字段 **/
    private BigDecimal price;
    private Integer stock;
    private String category;

    /** 内容特有字段 **/
    private String authorName;

    /** 静态构建方法：从实体快速转换 **/
    public static SearchResultDTO fromProduct(Product product) {
        if (product == null) return null;
        return new SearchResultDTO()
                .setId(product.getId())
                .setTitle(product.getName())
                .setContent(product.getDescription())
                .setImages(product.getImages())
                .setCreatedAt(product.getCreatedAt())
                .setPrice(product.getPrice())
                .setStock(product.getStock())
                .setCategory(product.getCategory())
                .setType("PRODUCT");
    }

    public static SearchResultDTO fromContent(CoffeeContent content) {
        if (content == null) return null;
        return new SearchResultDTO()
                .setId(content.getId())
                .setTitle(content.getTitle())
                .setContent(content.getContent())
                .setImages(content.getImages())
                .setCreatedAt(content.getCreatedAt())
                .setType("CONTENT");
    }
}
