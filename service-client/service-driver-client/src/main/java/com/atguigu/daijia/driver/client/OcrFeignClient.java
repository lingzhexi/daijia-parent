package com.atguigu.daijia.driver.client;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.driver.DriverLicenseOcrVo;
import com.atguigu.daijia.model.vo.driver.IdCardOcrVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "service-driver")
public interface OcrFeignClient {

    @PostMapping(value = "/ocr/idCardOcr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<IdCardOcrVo> idCardOcr(@RequestPart("file") MultipartFile file);


    @PostMapping(value = "/ocr/driverLicenseOcr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DriverLicenseOcrVo> driverLicenseOcr(@RequestPart("file") MultipartFile file);
}