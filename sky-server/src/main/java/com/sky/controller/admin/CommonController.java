package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.exception.UploadFailedException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件:{}", file);
        String url = null;
        try {
            //获得原始文件名
            String originalFilename = file.getOriginalFilename();
            //获得后缀名
            String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
            //UUID
            String objectName = UUID.randomUUID() + suffixName;
            //获得文件访问路径
            url = aliOssUtil.upload(file.getBytes(), objectName);
        } catch (IOException e) {
            throw new UploadFailedException(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success(url);
    }
}
