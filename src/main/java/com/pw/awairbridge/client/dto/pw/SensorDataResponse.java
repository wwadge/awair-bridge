package com.pw.awairbridge.client.dto.pw;

import java.util.List;
import lombok.Data;

@Data
public class SensorDataResponse {

  private boolean success;
  private List<SensorData> data;

  @Data
  public static class SensorData {
    private String sensorId;
  }
}
