package com.mika.bi.config;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ai.api")
@Data
public class AIApiConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public YuCongMingClient yuCongMingClient(){
        return new YuCongMingClient(accessKey,secretKey);
    }

}
