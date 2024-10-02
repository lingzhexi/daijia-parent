package com.atguigu.daijia.driver.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.config.TencentConfigProperties;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.driver.service.OcrService;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;
import com.atguigu.daijia.model.vo.driver.DriverLicenseOcrVo;
import com.atguigu.daijia.model.vo.driver.IdCardOcrVo;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.DriverLicenseOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.DriverLicenseOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.IDCardOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.IDCardOCRResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OcrServiceImpl implements OcrService {

    @Autowired
    private TencentConfigProperties tencentConfig;

    @Autowired
    private CosService cosService;

    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {
        try {
            // 1.获取 ocrClient 对象
            OcrClient client = tencentConfig.getOcrClient();

            // 2.实例化一个请求对象,每个接口都会对应一个request对象
            IDCardOCRRequest req = new IDCardOCRRequest();
            // 设置文件 Base64
            byte[] base64 = Base64.encodeBase64(file.getBytes());
            req.setImageBase64(new String(base64));

            // 3.返回的resp是一个IDCardOCRResponse的实例，与请求对象对应
            IDCardOCRResponse resp = client.IDCardOCR(req);
            log.info("身份证识别接口返回对象数据：{}", IDCardOCRResponse.toJsonString(resp));

            // 4.封装 IdCardOcrVo对象
            IdCardOcrVo idCardOcrVo = getIdCardOcrVo(file, resp);
            return idCardOcrVo;
        } catch (Exception e) {
            log.error("身份证接口异常日志：{}",e.getMessage());
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }

    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        try {
            // 1.获取 ocrClient 对象
            OcrClient client = tencentConfig.getOcrClient();

            // 2.实例化一个请求对象,每个接口都会对应一个request对象
            DriverLicenseOCRRequest req = new DriverLicenseOCRRequest();
            byte[] base64 = Base64.encodeBase64(file.getBytes());
            req.setImageBase64(new String(base64));

            // 3.返回的resp是一个 DriverLicenseOCRResponse 的实例，与请求对象对应
            DriverLicenseOCRResponse resp = client.DriverLicenseOCR(req);
            log.info("驾驶证识别接口返回对象数据：{}", DriverLicenseOCRRequest.toJsonString(resp));

            // 4.封装 DriverLicenseOcrVo 对象
            DriverLicenseOcrVo driverLicenseOcrVo = getDriverLicenseOcrVo(file, resp);
            return driverLicenseOcrVo;
        } catch (Exception e) {
            log.error("驾驶证接口异常日志：{}",e.getMessage());
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }

    /**
     * 身份证识别数据返回，封装IdCardOcrVo对象
     * 数据分正面和背面（是否有名字）
     *
     * @param file
     * @param resp
     * @return IdCardOcrVO 身份证识别数据
     */
    private IdCardOcrVo getIdCardOcrVo(MultipartFile file, IDCardOCRResponse resp) {
        IdCardOcrVo idCardOcrVo = new IdCardOcrVo();
        if (StringUtils.hasText(resp.getName())) {
            //身份证（人像面）
            idCardOcrVo.setName(resp.getName());
            idCardOcrVo.setGender("男".equals(resp.getSex()) ? "1" : "2");
            idCardOcrVo.setNation(resp.getNation());
            idCardOcrVo.setBirthday(DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(resp.getBirth()).toDate());
            idCardOcrVo.setIdcardNo(resp.getIdNum());
            idCardOcrVo.setIdcardAddress(resp.getAddress());

            //上传身份证正面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "idCard");
            idCardOcrVo.setIdcardFrontUrl(cosUploadVo.getUrl());
            idCardOcrVo.setIdcardFrontShowUrl(cosUploadVo.getShowUrl());
        } else {
            //身份证（国徽）
            //证件有效期："2010.07.21-2020.07.21"
            String idcardExpireString = resp.getValidDate().split("-")[1];
            idCardOcrVo.setIdcardExpire(DateTimeFormat.forPattern("yyyy.MM.dd").parseDateTime(idcardExpireString).toDate());
            idCardOcrVo.setAuthority(resp.getAuthority());

            //上传身份证反面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "idCard");
            idCardOcrVo.setIdcardBackUrl(cosUploadVo.getUrl());
            idCardOcrVo.setIdcardBackShowUrl(cosUploadVo.getShowUrl());
        }
        log.info("身份证对象数据返回：{}", JSON.toJSONString(idCardOcrVo));
        return idCardOcrVo;
    }

    /**
     * 驾驶证识别数据返回，封装IdCardOcrVo对象
     * 数据分正面和背面（是否有名字）
     *
     * @param file
     * @param resp
     * @return DriverLicenseOcrVo 驾驶证识别数据
     */
    private DriverLicenseOcrVo getDriverLicenseOcrVo(MultipartFile file, DriverLicenseOCRResponse resp) {
        DriverLicenseOcrVo driverLicenseOcrVo = new DriverLicenseOcrVo();
        if (StringUtils.hasText(resp.getName())) {
            //驾驶证正面
            //驾驶证名称要与身份证名称一致
            driverLicenseOcrVo.setName(resp.getName());
            driverLicenseOcrVo.setDriverLicenseClazz(resp.getClass_());
            driverLicenseOcrVo.setDriverLicenseNo(resp.getCardCode());
            driverLicenseOcrVo.setDriverLicenseIssueDate(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(resp.getDateOfFirstIssue()).toDate());
            driverLicenseOcrVo.setDriverLicenseExpire(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(resp.getEndDate()).toDate());

            //上传驾驶证反面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "driverLicense");
            driverLicenseOcrVo.setDriverLicenseFrontUrl(cosUploadVo.getUrl());
            driverLicenseOcrVo.setDriverLicenseFrontShowUrl(cosUploadVo.getShowUrl());
        } else {
            //驾驶证反面
            //上传驾驶证反面图片到腾讯云cos
            CosUploadVo cosUploadVo = cosService.upload(file, "driverLicense");
            driverLicenseOcrVo.setDriverLicenseBackUrl(cosUploadVo.getUrl());
            driverLicenseOcrVo.setDriverLicenseBackShowUrl(cosUploadVo.getShowUrl());

        }
        log.info("驾驶证对象数据返回：{}", JSON.toJSONString(driverLicenseOcrVo));
        return driverLicenseOcrVo;
    }
}
