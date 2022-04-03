package com.pw.awairbridge.client.dto.pw;

import com.pw.awairbridge.client.dto.awair.AwairDataPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true)
public class PWData extends AwairDataPacket {
  private String deviceId;
}
