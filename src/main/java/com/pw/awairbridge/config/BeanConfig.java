package com.pw.awairbridge.config;

import com.pw.awairbridge.client.AwairClient;
import com.pw.awairbridge.client.PWAuthClient;
import com.pw.awairbridge.client.PWClient;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Slf4j
@Import(FeignClientsConfiguration.class)
public class BeanConfig {


  public static volatile String accessToken;

  @Bean
  AwairClient awairLocalClient(Encoder encoder, Decoder decoder, Contract contract, @Value("${awair.address}") String url) {
    return Feign.builder()
        .encoder(encoder)
        .decoder(decoder)
        .contract(contract)
        .target(AwairClient.class, url);
  }

  @Bean
  PWClient pwClient(Encoder encoder, Decoder decoder, Contract contract, @Value("${pw.address:https://wearableapi.planetwatch.io/}") String url) {
    return Feign.builder()
        .encoder(encoder)
        .decoder(decoder)
        .contract(contract)
        .requestInterceptor(template -> {
          template.header("Authorization", "Bearer "+accessToken);
        })
        .target(PWClient.class, url);
  }


  @Bean
  PWAuthClient pwAuthClient(Encoder encoder, Decoder decoder, Contract contract) {
    return Feign.builder()
        .encoder(encoder)
        .decoder(decoder)
        .contract(contract)
        .encoder(new FormEncoder())
        .target(PWAuthClient.class, "https://login.planetwatch.io");
  }

  @Bean
  public RegistryEventConsumer<Retry> customRetryRegistryEventConsumer() {
    // log resilience retry events
    return new RegistryEventConsumer<>() {
      @Override
      public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
            .onEvent(event -> log.info(event.toString()));
      }

      @Override
      public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {}

      @Override
      public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {}
    };
  }


}
