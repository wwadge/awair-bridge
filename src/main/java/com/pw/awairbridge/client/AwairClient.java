package com.pw.awairbridge.client;

import com.pw.awairbridge.model.AwairData;
import org.springframework.web.bind.annotation.GetMapping;

public interface AwairClient {

  @GetMapping(produces = "application/json", value = "/air-data/latest")
  AwairData getAirData();

}
