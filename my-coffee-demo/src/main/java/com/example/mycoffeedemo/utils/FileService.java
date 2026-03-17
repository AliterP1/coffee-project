package com.example.mycoffeedemo.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    public Result<String> upload(MultipartFile file, String type)  {
        try {
            // 1. 确保目录存在
            File dir = new File(DataUtils.UPLOAD_DIR + type);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 2.检查文件是否为空
            if (file.isEmpty()) {
                return Result.fail(400,"文件为空");
            }
            // 3. 生成唯一文件名
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            // 4. 保存文件
            file.transferTo(dest);
            String url = "/uploads/" + type + "/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(500, "文件上传失败");
        }
    }

    public void deleteFile(String filename){
        File file = new File(DataUtils.DELETE_DIR + filename);
        if (file.exists()){
            file.delete();
        }
    }
}
