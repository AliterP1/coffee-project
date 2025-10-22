package com.example.mycoffeedemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mycoffeedemo.dto.CoffeeContentResponseDTO;
import com.example.mycoffeedemo.dto.SearchResultDTO;
import com.example.mycoffeedemo.entity.CoffeeContent;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface CoffeeContentService extends IService<CoffeeContent> {
    Result<IPage<CoffeeContentResponseDTO>> getCoffeeContentPage(Long page, Long size);

    Result<List<String>> uploadCoffeeImages(String id, List<MultipartFile> file) throws JsonProcessingException;

    Result<IPage<SearchResultDTO>> searchCoffeeContent(Long page, Long size, String search);
}
