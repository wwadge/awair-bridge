package com.pw.awairbridge.client.dto.awair;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AwairDeviceResponse {
  List<AwairDevice> devices;
  private String bearerToken;


  @Data
  public static class AwairDevice {
    private String bearerToken;

    private String name;
    private BigDecimal latitude; // 37.17
    private BigDecimal longitude; //  122.4
    private String preference; // GENERAL
    private String timezone; // US/Pacific
    private String roomType; // BEDROOM
    private String deviceType; // awair-element
    private String spaceType; // HOME
    private String deviceUUID; // awair_0
    private Integer deviceId; // 0
    private String locationName; // My Home
  }
}
