package com.pw.awairbridge.config;

import com.pw.awairbridge.client.AwairClient;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FeignClientsConfiguration.class)
public class BeanConfig {

  @Bean
  AwairClient awairClient(Encoder encoder, Decoder decoder, Contract contract, @Value("${awair.address}") String url) {
    // Explicitly listing this here instead of via @EnableFeignClients means it will work if we
    // do native compilation
    return Feign.builder()
        .encoder(encoder)
        .decoder(decoder)
        .contract(contract)
        .target(AwairClient.class, "http://"+url);
  }

}
