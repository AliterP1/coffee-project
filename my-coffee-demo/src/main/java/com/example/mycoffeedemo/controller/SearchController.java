package com.example.mycoffeedemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mycoffeedemo.dto.CoffeeContentResponseDTO;
import com.example.mycoffeedemo.dto.SearchResultDTO;
import com.example.mycoffeedemo.service.CoffeeContentService;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private CoffeeContentService coffeeContentService;
    @GetMapping
    public Result<IPage<SearchResultDTO>> searchCoffeeContent(@RequestParam(defaultValue = "1") Long page,
                                                             @RequestParam(defaultValue = "10") Long size,
                                                             @RequestParam String search){
        return coffeeContentService.searchCoffeeContent(page,size,search);
    }
}
