package com.pw.awairbridge.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.pw.awairbridge.client.AwairClient;
import com.pw.awairbridge.client.PWClient;
import com.pw.awairbridge.persistence.FirebasePersistence;
import com.pw.awairbridge.persistence.TokenPersistence;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
  AwairClient awairLocalClient(Encoder encoder, Decoder decoder, Contract contract, @Value("${awair.address}") String url, @Value("${awair.token}") String token) {
    return Feign.builder()
        .encoder(encoder)
        .decoder(decoder)
        .contract(contract)
        .requestInterceptor(template -> template.header("Authorization", "Bearer "+token))
        .target(AwairClient.class, url);
  }

  @Bean
  PWClient pwClient(Encoder encoder, Decoder decoder, Contract contract, @Value("${pw.address}") String url) {
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
  @ConditionalOnProperty(name = "persistence.type", havingValue = "memory")
  TokenPersistence memory(@Value("${pw.initialRefreshToken}") String refreshToken) {
    log.info("Using in-memory storage");

    return new TokenPersistence() {
        volatile String token = null;

        @Override
        public void store(String refreshToken) {
          this.token = refreshToken;
        }

        @Override
        public String load() {
          return token == null ? refreshToken : token;
        }
      };
  }

  @Bean
  @ConditionalOnProperty(name = "persistence.type", havingValue = "firebase")
  TokenPersistence google(@Value("${persistence.fireStoreProjectId}") String projectId, @Value("${persistence.serviceAccountFile}") String serviceAccountFile, @Value("${pw.initialRefreshToken}") String refreshToken) throws IOException {
    log.info("Using google storage");

    InputStream serviceAccount = new FileInputStream(serviceAccountFile);
    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .setProjectId(projectId)
        .build();

    FirebaseApp.initializeApp(options);

    Firestore firestore = FirestoreClient.getFirestore();
    return new FirebasePersistence(firestore, refreshToken);
  }

  @Bean
  @ConditionalOnProperty(name = "persistence.type", havingValue = "google")
  TokenPersistence firebase(@Value("${persistence.fireStoreProjectId}") String projectId, @Value("${pw.initialRefreshToken}") String refreshToken) throws IOException {

    log.info("Using firebase storage");
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .setProjectId(projectId)
        .build();

    FirebaseApp.initializeApp(options);

    Firestore firestore = FirestoreClient.getFirestore();
    return new FirebasePersistence(firestore, refreshToken);
  }

  @Bean
  @ConditionalOnProperty(name = "persistence.type", havingValue = "local")
  TokenPersistence localStorage( @Value("${pw.initialRefreshToken}") String refreshToken, @Value("${persistence.localFile}") String localFile){
    log.info("Using local storage");

    return new TokenPersistence() {
      @Override
      public void store(String refreshToken) {
        try {
          Files.writeString(Path.of(localFile), refreshToken);
        } catch (IOException e) {
          log.error("Unable to write to local file", e);
        }
      }

      @Override
      public String load() {
        String result = refreshToken;
        try {
          result = Files.readString(Path.of(localFile));
        } catch (IOException e) {
          log.warn("Unable to load {}, using initial refresh token from config", localFile);
        }

        return result;
      }
    };
  }

}
