package com.pw.awairbridge.client.dto.pw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Auth {

  @JsonProperty("access_token")
  String accessToken;
  @JsonProperty("refresh_token")
  String refreshToken;

}
