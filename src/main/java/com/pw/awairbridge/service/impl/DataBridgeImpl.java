package com.pw.awairbridge.service.impl;

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
import com.pw.awairbridge.persistence.TokenPersistence;
import com.pw.awairbridge.service.DataBridge;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component @Slf4j
public class DataBridgeImpl implements DataBridge {

  final AwairClient awairClient;
  final PWClient pwClient;
  private final URI authUrl;
  Auth auth = new Auth();

  RestTemplate restTemplate = new RestTemplate();
  final TokenPersistence tokenPersistence;
  private List<AwairDevice> matchingPWSensors;


  public DataBridgeImpl(AwairClient awairClient, PWClient pwClient,
      @Value("${pw.initialRefreshToken}") String refreshToken,
      @Value("${pw.authUrl:https://login.planetwatch.io/auth/realms/Planetwatch/protocol/openid-connect/token}") URI authUrl,
      TokenPersistence tokenPersistence) {
    this.awairClient = awairClient;
    this.pwClient = pwClient;
    this.tokenPersistence = tokenPersistence;
    this.auth.setRefreshToken(refreshToken);
    this.authUrl = authUrl;
    refreshToken(); // call it at least once
    filterPWEnabledSensors(); // figure out which sensors we care about

  }

  @Scheduled(cron = "${pw.authRefreshRateCron:0 0/10 * * * *}")
  public void refreshToken() {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", "external-login");
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", tokenPersistence.load());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Accept", "application/json");
    headers.add("User-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36");

    HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<Auth> res = restTemplate.exchange(authUrl, HttpMethod.POST, entity, Auth.class);

    if (res.getStatusCode() == HttpStatus.OK){
      this.auth = res.getBody();
      tokenPersistence.store(this.auth.getRefreshToken());
      BeanConfig.accessToken = this.auth.getAccessToken();
      log.info("Auth token has been refreshed");
    } else {
      log.error("Failed attempting to fetch refresh token. Status code: {}", res.getStatusCode());
    }

  }

  public void filterPWEnabledSensors(){

    // Fetch the sensors that PW knows about
    SensorDataResponse sensors = pwClient.getSensors();
    log.info("PW Sensors: " +sensors.toString());

    // Fetch the sensors registered by awair
    AwairDeviceResponse awairDevices = awairClient.getDeviceList();
    log.info("Awair Sensors: " +awairDevices.toString());

    // Filter the list provided by Awair to only include the stuff that
    // PW knows about
    this.matchingPWSensors = awairDevices.getDevices().stream()
        .filter(p-> sensors.getData().stream()
            .map(SensorData::getSensorId)
            .anyMatch(name -> name.equals(p.getDeviceUUID())))
            .collect(Collectors.toList());
  }

  @Override
  @Scheduled(cron = "${awair.fetchRateCron:0 0/15 * * * *}")
  public void runService() throws InterruptedException {

    log.info("Starting data fetch...");
    for (AwairDevice device: this.matchingPWSensors) {
      AwairDataPacket awairData = awairClient.getAirData(device.getDeviceType(), device.getDeviceId());
      log.info(awairData.toString());
      PWData pwData = new PWData();

      List<Map<String, Object>> data = (List<Map<String, Object>>) awairData.getOtherFields().get("data");
      pwData.setOtherFields( data.get(0));
      pwData.setDeviceId(device.getDeviceUUID());
      PWDataResponse pwResponse = pwClient.sendData(pwData);
      log.info("Sending data for device '{}' to PW: {}", device.getName(), pwResponse.isSuccess() ? "OK" : "FAILED");
      Thread.sleep(1000L);  // let's be nice to pw and awair and not flood their servers
    }

  }
}
