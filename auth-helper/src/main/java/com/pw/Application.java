package com.pw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    // This is non-default java cipher list to bypass cloudflare TLS fingerprinting
    System.setProperty("https.cipherSuites", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256");
    System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36");
    System.setProperty("https.agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36");

    SpringApplication.run(Application.class, args);
  }

}
