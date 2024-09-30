package com.atguigu.daijia.driver.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WxConfigOperator {
    @Autowired
    private WxConfigProperties wxConfigProperties;

    @Bean
    public WxMaService wxMaService() {

        //微信默认配置小程序密钥和appid
        WxMaDefaultConfigImpl wxMaDefaultConfig = new WxMaDefaultConfigImpl();
        wxMaDefaultConfig.setSecret(wxConfigProperties.getSecret());
        wxMaDefaultConfig.setAppid(wxConfigProperties.getAppId());

        //配置绑定到微信服务
        WxMaServiceImpl wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaDefaultConfig);

        //返回微信服务
        return wxMaService;
    }

}
