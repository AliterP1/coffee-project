package com.example.mycoffeedemo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.CoffeeContentResponseDTO;
import com.example.mycoffeedemo.dto.SearchResultDTO;
import com.example.mycoffeedemo.entity.CoffeeContent;
import com.example.mycoffeedemo.entity.Product;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.mapper.CoffeeContentMapper;
import com.example.mycoffeedemo.mapper.ProductMapper;
import com.example.mycoffeedemo.mapper.UserMapper;
import com.example.mycoffeedemo.service.CoffeeContentService;
import com.example.mycoffeedemo.service.FileService;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoffeeContentServiceImpl extends ServiceImpl<CoffeeContentMapper, CoffeeContent> implements CoffeeContentService {

    @Autowired
    private CoffeeContentMapper coffeeContentMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Result<IPage<CoffeeContentResponseDTO>> getCoffeeContentPage(Long page, Long size) {
        Page<CoffeeContent> pageParam = new Page<>(page, size);
        IPage<CoffeeContent> contentPage = coffeeContentMapper.selectPage(pageParam, new QueryWrapper<>());

        // 转 DTO
        IPage<CoffeeContentResponseDTO> dtoPage = contentPage.convert(content -> {
            CoffeeContentResponseDTO dto = new CoffeeContentResponseDTO();
            BeanUtils.copyProperties(content, dto);
            // 将 JSON 字符串解析为 List<String>
            if (content.getImages() != null) {
                dto.setImages(Arrays.stream(content.getImages()
                        .replace("[","")
                        .replace("]","")
                        .replace("\"","")
                        .split(","))
                        .map(String::trim).collect(Collectors.toList()));
            }
            // 查作者名字
            if (content.getAuthorId() != null) {
                User author = userMapper.selectById(content.getAuthorId());
                if (author != null) {
                    dto.setAuthorName(author.getUsername());
                }
            }
            return dto;
        });

        return Result.success("获取成功",dtoPage);
    }

    @Override
    public Result<List<String>> uploadCoffeeImages(String id, List<MultipartFile> files) throws JsonProcessingException {
        ArrayList<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            Result<String> uploadResult = fileService.upload(file, "content");
            if (uploadResult.getCode() == 200) {
                urls.add(uploadResult.getData());
            }else {
                Result.fail("文件上传失败");
            }
        }
        CoffeeContent content = coffeeContentMapper.selectById(id);
        content.setImages(new ObjectMapper().writeValueAsString(urls));
        coffeeContentMapper.updateById(content);
        return Result.success("图片上传成功",urls);
    }

    @Override
    public Result<IPage<SearchResultDTO>> searchCoffeeContent(Long page, Long size, String search) {
        if (StringUtils.isBlank(search)) {
            return Result.fail("请勿输入空白进行搜索");
        }

        // 改为 selectList（一次查全量，防止第二页空）
        List<CoffeeContent> contentList = coffeeContentMapper.selectList(
                new LambdaQueryWrapper<CoffeeContent>()
                        .and(w -> w
                                .like(CoffeeContent::getTitle, search)
                                .or()
                                .like(CoffeeContent::getContent, search))
                        .orderByDesc(CoffeeContent::getCreatedAt)
        );

        List<Product> productList = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getIsActive, true)
                        .and(w -> w
                                .like(Product::getName, search)
                                .or()
                                .like(Product::getDescription, search)
                                .or()
                                .like(Product::getCategory, search))
                        .orderByDesc(Product::getCreatedAt)
        );

        // === 封装结果 ===
        List<SearchResultDTO> allResults = new ArrayList<>();

        // 内容结果封装
        if (CollectionUtils.isNotEmpty(contentList)) {
            List<SearchResultDTO> contentDTOs = contentList.stream()
                    .map(c -> {
                        SearchResultDTO dto = SearchResultDTO.fromContent(c);

                        // 图片 JSON -> List<String>
                        dto.setImages(parseImageList(c.getImages()));

                        // 查找用户名
                        if (c.getAuthorId() != null) {
                            User user = userMapper.selectById(c.getAuthorId());
                            dto.setAuthorName(user != null ? user.getUsername() : "未知用户");
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());
            allResults.addAll(contentDTOs);
        }

        // 商品结果封装
        if (CollectionUtils.isNotEmpty(productList)) {
            List<SearchResultDTO> productDTOs = productList.stream()
                    .map(p -> {
                        SearchResultDTO dto = SearchResultDTO.fromProduct(p);
                        dto.setImages(parseImageList(p.getImages()));
                        return dto;
                    })
                    .collect(Collectors.toList());
            allResults.addAll(productDTOs);
        }

        // 合并排序（按创建时间倒序）
        allResults.sort(Comparator.comparing(SearchResultDTO::getCreatedAt).reversed());

        // === 手动分页 ===
        int total = allResults.size();
        int start = (int) ((page - 1) * size);

        // 越界保护
        if (start >= total) {
            Page<SearchResultDTO> emptyPage = new Page<>(page, size, total);
            emptyPage.setRecords(Collections.emptyList());
            return Result.success(emptyPage);
        }

        int end = Math.min(start + size.intValue(), total);
        List<SearchResultDTO> pagedList = allResults.subList(start, end);

        // 构造分页对象
        Page<SearchResultDTO> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(pagedList);

        return Result.success(resultPage);
    }

    /**
     * 工具方法：将 JSON 数组字符串转为 List<String>
     * 示例输入："[\"a.jpg\",\"b.jpg\"]"
     */
    public static List<String> parseImageList(String images) {
        if (StringUtils.isBlank(images)) {
            return Collections.emptyList();
        }
        return Arrays.stream(
                        images.replaceAll("[\\[\\]\"]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

}
