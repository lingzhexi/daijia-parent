package com.atguigu.daijia.driver.service;

import com.atguigu.daijia.model.vo.driver.CosUploadVo;
import org.springframework.web.multipart.MultipartFile;

public interface CosService {

    CosUploadVo upload(MultipartFile file, String path);

    String getImageUrl(String path);
}
