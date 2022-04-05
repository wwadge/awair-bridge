package com.pw.awairbridge.util;

import com.pw.awairbridge.client.PWAuthClient;
import com.pw.awairbridge.client.dto.pw.Auth;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j @Component
public class PWAuthService {

  public static final String DUMMY_REDIRECT = "http://localhost:33333/keycloak-redirect";
  public static final String KC_CLIENT = "external-login";
  public static final String CLIENT_ID = "client_id";
  public static final String REDIRECT_URI = "redirect_uri";
  public static final String RESPONSE_TYPE = "response_type";
  public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36";
  private final String username;
  private final String password;
  final PWAuthClient authClient;

  public PWAuthService(@Value("${pw.username}") String username,
      @Value("${pw.password}") String password, PWAuthClient authClient){
    this.username = username;
    this.password = password;
    this.authClient = authClient;
  }
  public Auth doLogin() throws IOException {
    log.info("Logging in PW");

    ResponseEntity<String> authResponse = authClient.openIDAuth(
        Map.of(CLIENT_ID, KC_CLIENT,
            REDIRECT_URI, DUMMY_REDIRECT,
            "scope", "openid offline_access",
            RESPONSE_TYPE, "code" ));


    if (authResponse.getStatusCode() != HttpStatus.OK){
      throw new IOException("Could not start PW auth. Perhaps Cloudflare is blocking your request (especially if you're on cloud)");
    }


    String loginAction = Jsoup.parse(authResponse.getBody()).select("#kc-form-login").attr("action").replace("&amp;", "&");

    Map<String, String> mapOfCookies = authResponse.getHeaders().get("Set-Cookie").stream().collect(
        Collectors.toMap(s -> s.split("=")[0], s -> s.split("=")[1]
        ));


    Response response = Jsoup.connect(loginAction)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .method(Method.POST)
        .followRedirects(false)
        .header("Accept", "*/*")
        .header("user-agent", USER_AGENT)
        .data("username", username)
        .data("password", password)
        .cookies(mapOfCookies)
        .execute();



    if (response.statusCode() != 302){
      throw new IOException("Could not login to PW. Probably bad username/password. Check your config");
    }

    List<NameValuePair> params = URLEncodedUtils.parse(response.header("Location"), Charset.forName("UTF-8"));

    Optional<NameValuePair> code = params.stream().filter(p-> p.getName().equals("code")).findAny();


    ResponseEntity<Auth> res = authClient.fetchToken(    Map.of(CLIENT_ID, KC_CLIENT,
        "grant_type", "authorization_code",
        "code", code.get().getValue(),
        REDIRECT_URI, DUMMY_REDIRECT
        )
    );


    if (res.getStatusCode() == HttpStatus.OK) {
      log.info("Login to PW: OK");
    } else {
      log.error("Login to PW: FAILED");
    }
    return res.getBody();
  }


}
