package com.atguigu.daijia.driver.controller;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "腾讯云cos上传接口管理")
@RestController
@RequestMapping(value = "/cos")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosController {
    @Autowired
    private CosService cosService;

    @Operation(summary = "上传文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CosUploadVo> upload(@RequestPart("file") MultipartFile file,
                                      @RequestParam(name = "path", defaultValue = "auth") String path) {
        return Result.ok(cosService.upload(file, path));
    }

    @Operation(summary = "查看临时图片url")
    @GetMapping("/getImageUrl")
    public Result<String> getImageUrl(String path) {
        return Result.ok(cosService.getImageUrl(path));
    }
}

