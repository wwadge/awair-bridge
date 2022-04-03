package com.pw.awairbridge.client;

import com.pw.awairbridge.client.dto.pw.PWData;
import com.pw.awairbridge.client.dto.pw.PWDataResponse;
import com.pw.awairbridge.client.dto.pw.SensorDataResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public interface PWClient {

  @GetMapping(produces = "application/json", value = "/api/sensors")
  @Retry(name = "pw-getsensors")
  SensorDataResponse getSensors();

  @PostMapping(produces = "application/json", value = "/api/data/devicedata")
  @Retry(name = "pw-send-data")
  PWDataResponse sendData(PWData pwData);

}
