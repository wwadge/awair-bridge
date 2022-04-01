package com.pw.awairbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AwairBridgeApplication {

  public static void main(String[] args) {
      SpringApplication.run(AwairBridgeApplication.class, args);
  }

}
