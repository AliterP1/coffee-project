package com.example.mycoffeedemo.config;


import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.example.mycoffeedemo.entity.AliPayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {

    @Bean
    public Config config(AliPayProperties payProperties){
        Config config = new Config();
        config.protocol = payProperties.getProtocol();
        config.gatewayHost = payProperties.getGatewayHost();
        config.signType = payProperties.getSignType();
        config.appId = payProperties.getAppId();
        config.merchantPrivateKey = payProperties.getMerchantPrivateKey();
        config.alipayPublicKey = payProperties.getAlipayPublicKey();

        config.notifyUrl = payProperties.getNotifyUrl();
        config.encryptKey="";

        //初始配置
        Factory.setOptions(config);
        return config;
    }
}
