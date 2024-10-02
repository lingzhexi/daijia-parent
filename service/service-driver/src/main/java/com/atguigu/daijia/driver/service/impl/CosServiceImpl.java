package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.config.TencentConfigProperties;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosServiceImpl implements CosService {

    @Autowired
    private TencentConfigProperties tencentConfig;

    @Override
    public CosUploadVo upload(MultipartFile file, String path) {
        COSClient cosClient = tencentConfig.getPrivateCosClient();

        //元数据信息
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentEncoding("UTF-8");
        meta.setContentType(file.getContentType());

        //向存储桶中保存文件
        String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));//获取后缀
        String uploadPath = "/driver/" + path + "/" + UUID.randomUUID().toString().replaceAll("-", "") + fileType;
        //封装文件上传请求对象
        PutObjectRequest putObjectRequest = null;
        try {
            putObjectRequest = new PutObjectRequest(tencentConfig.getBucketPrivate(), uploadPath, file.getInputStream(), meta);
            putObjectRequest.setStorageClass(StorageClass.Standard);

            //文件上传到腾讯cos对象存储
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            log.info("putObjectResult: {}",putObjectResult);
            //封装返回对象
            CosUploadVo cosUploadVo = new CosUploadVo();
            cosUploadVo.setUrl(uploadPath);
            //图片临时访问url，回显使用
            String imageUrl = this.getImageUrl(uploadPath);
            cosUploadVo.setShowUrl(imageUrl);
            return cosUploadVo;
        } catch (IOException e) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public String getImageUrl(String path) {
        if (!StringUtils.hasText(path)) return "";
        // 获取COSClient对象
        COSClient cosClient = tencentConfig.getPrivateCosClient();
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(tencentConfig.getBucketPrivate(), path, HttpMethodName.GET);
        Date expiration = new DateTime().plusMinutes(15).toDate();

        // 设置临时过期时间 15分钟
        request.withExpiration(expiration);
        // 调用方法
        URL url = cosClient.generatePresignedUrl(request);
        cosClient.shutdown();
        return url.toString();
    }

}
