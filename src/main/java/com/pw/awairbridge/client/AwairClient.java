package com.pw.awairbridge.client;

import com.pw.awairbridge.client.dto.awair.AwairDataPacket;
import com.pw.awairbridge.client.dto.awair.AwairDeviceResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Retry(name = "awair")
public interface AwairClient {

  @GetMapping(produces = "application/json", value = "/v1/users/self/devices/{deviceType}/{deviceId}/air-data/latest")
  AwairDataPacket getAirData(@PathVariable(name = "deviceType") String deviceType, @PathVariable(name = "deviceId") Integer deviceId);

  @GetMapping(produces = "application/json", value = "/v1/users/self/devices")
  AwairDeviceResponse getDeviceList();

}
