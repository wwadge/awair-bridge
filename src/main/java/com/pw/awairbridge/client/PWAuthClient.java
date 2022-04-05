package com.pw.awairbridge.client;

import static com.pw.awairbridge.util.PWAuthService.USER_AGENT;

import com.pw.awairbridge.client.dto.pw.Auth;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Retry(name = "pw-auth")
public interface PWAuthClient {

  @PostMapping(consumes = "application/x-www-form-urlencoded",
      headers = {"user-agent="+ USER_AGENT},
       value = "/auth/realms/Planetwatch/protocol/openid-connect/auth")
  ResponseEntity<String> openIDAuth(@RequestBody Map<String, ?> form);



  @PostMapping(consumes = "application/x-www-form-urlencoded", produces = "application/json",
      headers = {"user-agent="+ USER_AGENT},
      value = "/auth/realms/Planetwatch/protocol/openid-connect/token")
  ResponseEntity<Auth> fetchToken(@RequestBody Map<String, ?> form);

}
