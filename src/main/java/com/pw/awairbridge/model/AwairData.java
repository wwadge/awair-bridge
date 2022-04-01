package com.pw.awairbridge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class AwairData {

  OffsetDateTime timestamp;

  int score;

  @JsonProperty("dew_point")
  BigDecimal dewPoint;

  @JsonProperty("temp")
  BigDecimal temperature;

  @JsonProperty("humid")
  BigDecimal humidity;

  @JsonProperty("abs_humid")
  BigDecimal absoluteHumidity;

  Integer co2;

  @JsonProperty("co2_est")
  Integer co2Est;

  @JsonProperty("co2_est_baseline")
  Integer co2EstBasline;

  Integer voc;

  @JsonProperty("boc_baseline")
  Integer vocBaseline;

  @JsonProperty("voc_h2_raw")
  Integer vocH2Raw;

  @JsonProperty("voc_ethanol_raw")
  Integer vocEthanolRaw;

  Integer pm25;

  @JsonProperty("pm10_est")
  Integer pm10Est;

}
