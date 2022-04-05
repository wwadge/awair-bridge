package com.pw.awairbridge.service.impl;

import com.google.common.collect.Maps;
import com.pw.awairbridge.client.AwairClient;
import com.pw.awairbridge.client.PWClient;
import com.pw.awairbridge.client.dto.awair.AwairDataPacket;
import com.pw.awairbridge.client.dto.awair.AwairDeviceResponse;
import com.pw.awairbridge.client.dto.awair.AwairDeviceResponse.AwairDevice;
import com.pw.awairbridge.client.dto.pw.Auth;
import com.pw.awairbridge.client.dto.pw.PWData;
import com.pw.awairbridge.client.dto.pw.PWDataResponse;
import com.pw.awairbridge.client.dto.pw.SensorDataResponse;
import com.pw.awairbridge.client.dto.pw.SensorDataResponse.SensorData;
import com.pw.awairbridge.config.BeanConfig;
import com.pw.awairbridge.service.DataBridge;
import com.pw.awairbridge.util.PWAuthService;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class DataBridgeImpl implements DataBridge {

  final AwairClient awairClient;
  final PWClient pwClient;

  final List<String> awairTokens;

  private Set<AwairDevice> matchingPWSensors;
  final PWAuthService pwAuthService;

  public DataBridgeImpl(AwairClient awairClient, PWClient pwClient,
      @Value("${awair.token}")
          List<String> awairTokens, PWAuthService pwAuthService) throws IOException {
    this.awairClient = awairClient;
    this.pwClient = pwClient;
    this.awairTokens = awairTokens;
    this.pwAuthService = pwAuthService;

    Auth pwAuth = pwAuthService.doLogin();
    BeanConfig.accessToken = pwAuth.getAccessToken();

    filterPWEnabledSensors(); // figure out which sensors we care about

  }

  @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.MINUTES, initialDelay = 20)
  public void doLogin() throws IOException {
    Auth pwAuth = pwAuthService.doLogin();
    BeanConfig.accessToken = pwAuth.getAccessToken();
  }

  public void filterPWEnabledSensors(){

    // Fetch the sensors that PW knows about
    SensorDataResponse sensors = pwClient.getSensors();
    log.info("PW Sensors: " +sensors.toString());

    Map<String, AwairDeviceResponse> allAwairDevices = Maps.newHashMap();
    // Fetch the sensors registered by awair
    for (String bearerToken: awairTokens){
      AwairDeviceResponse devices = awairClient.getDeviceList("Bearer "+bearerToken);
      devices.setBearerToken(bearerToken);

      allAwairDevices.put(bearerToken, devices);
      log.info("Awair Sensors: " +devices.toString());
    }



    // Filter the list provided by Awair to only include the stuff that
    // PW knows about

    this.matchingPWSensors = new HashSet<>();

    allAwairDevices.entrySet().forEach(c -> {
      List<AwairDevice> matchedDevices = c.getValue().getDevices().stream()
          .filter(p -> sensors.getData().stream()
              .map(SensorData::getSensorId)
              .anyMatch(name -> name.equals(p.getDeviceUUID())))
          .collect(Collectors.toList());

      matchedDevices.forEach(p-> p.setBearerToken(c.getKey()));
      this.matchingPWSensors.addAll(matchedDevices);
    });
  }

  @Override
  @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
  @SuppressWarnings("unchecked")
  public void runService() throws InterruptedException {

    log.info("Starting data fetch...");
    for (AwairDevice device: this.matchingPWSensors) {
      AwairDataPacket awairData = awairClient.getAirData("Bearer "+device.getBearerToken(), device.getDeviceType(), device.getDeviceId());
      log.info(awairData.toString());
      PWData pwData = new PWData();

      List<Map<String, Object>> data = (List<Map<String, Object>>) awairData.getOtherFields().get("data");
      if (data.isEmpty()){
        log.warn("Did not receive data as expected from Awair, skipping '{}'", device.getName());
      } else {
        pwData.setOtherFields(data.get(0));
        pwData.setDeviceId(device.getDeviceUUID());
        PWDataResponse pwResponse = pwClient.sendData(pwData);
        log.info("Sending data for device '{}' to PW: {}", device.getName(),
            pwResponse.isSuccess() ? "OK" : "FAILED");
        Thread.sleep(10000L);  // let's be nice to pw and awair and not flood their servers, we're not in a hurry here
      }
    }

  }
}
