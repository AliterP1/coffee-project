package com.example.mycoffeedemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mycoffeedemo.dto.CoffeeContentResponseDTO;
import com.example.mycoffeedemo.service.CoffeeContentService;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/coffee")
public class CoffeeContentController {
    @Autowired
    private CoffeeContentService coffeeContentService;

    @GetMapping
    public Result<IPage<CoffeeContentResponseDTO>> getCoffeeContent(@RequestParam(defaultValue = "1") Long page,
                                                                    @RequestParam(defaultValue = "10") Long size){
        Result<IPage<CoffeeContentResponseDTO>> coffeeContentPage = coffeeContentService.getCoffeeContentPage(page, size);
        return coffeeContentPage;
    }

    @PostMapping("/{id}/images")
    public Result<List<String>> uploadCoffeeImages(@PathVariable String id, @RequestParam("files") List<MultipartFile> files) throws JsonProcessingException {
        return coffeeContentService.uploadCoffeeImages(id,files);
    }
}
