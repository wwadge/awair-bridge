package com.pw.awairbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AwairBridgeApplication {

  public static void main(String[] args) {
    // This is non-default java cipher list to bypass cloudflare TLS fingerprinting
    System.setProperty("https.cipherSuites", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");

    SpringApplication.run(AwairBridgeApplication.class, args);
  }

}
