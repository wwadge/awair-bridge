package com.pw.awairbridge.service.impl;

import com.pw.awairbridge.client.AwairClient;
import com.pw.awairbridge.model.AwairData;
import com.pw.awairbridge.service.DataBridge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class DataBridgeImpl implements DataBridge {

  final AwairClient awairClient;

  public DataBridgeImpl(AwairClient awairClient) {
    this.awairClient = awairClient;
  }

  @Override
  @Scheduled(fixedRateString = "${awair.fetchRate}")
  public AwairData fetchData() {

    AwairData awairData = awairClient.getAirData();
    log.info(awairData.toString());
    return awairData;
  }
}
