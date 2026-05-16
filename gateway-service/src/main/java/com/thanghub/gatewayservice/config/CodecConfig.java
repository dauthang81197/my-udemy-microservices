package com.thanghub.gatewayservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class CodecConfig implements WebFluxConfigurer {

    private static final int MAX_SIZE = 500 * 1024 * 1024; // 500MB

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(MAX_SIZE);
    }
}
