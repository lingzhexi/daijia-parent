package com.atguigu.daijia.driver.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.cloud")
public class TencentConfigProperties {
    private String secretId;
    private String secretKey;
    private String region;
    private String bucketPrivate;


    /**
     * 创建cos客户端
     * @return CosClient
     */
    @Bean
    public COSClient getPrivateCosClient() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        String secretId = this.getSecretId();
        String secretKey = this.getSecretKey();
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置 bucket 的地域, COS 地域
        Region region = new Region(this.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }

    /**
     * 创建Ocr客户端
     * @return
     */
    @Bean
    public OcrClient getOcrClient() {
        // 1.实例化一个认证对象，传入 SecretId 和 SecretKey
        Credential cred = new Credential(this.getSecretId(), this.getSecretKey());
        // 2.实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ocr.tencentcloudapi.com");
        // 3.实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 4.实例化要请求产品的client对象,clientProfile是可选的
        OcrClient client = new OcrClient(cred, this.getRegion(), clientProfile);
        return client;
    }

}
