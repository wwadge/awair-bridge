package com.pw.awairbridge.util;

import com.pw.awairbridge.client.dto.pw.Auth;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

  public PWAuthService(@Value("${pw.username}") String username, @Value("${pw.password}") String password){
    this.username = username;
    this.password = password;
  }
  public Auth doLogin() throws IOException {
    log.info("Logging in PW");
    Connection connection = Jsoup.connect("https://login.planetwatch.io/auth/realms/Planetwatch/protocol/openid-connect/auth");

    Response response = connection.header("Content-Type",
            "application/x-www-form-urlencoded")
        .method(Method.GET)
        .header("user-agent", USER_AGENT)
        .data(CLIENT_ID, KC_CLIENT)
        .data(REDIRECT_URI, DUMMY_REDIRECT)
        .data("scope", "openid offline_access")
        .data(RESPONSE_TYPE, "code")
        .execute();


    if (response.statusCode() != 200){
      throw new IOException("Could not start PW auth. Perhaps Cloudflare is blocking your request (especially if you're on cloud)");
    }

    String loginAction = response.parse().select("#kc-form-login").attr("action").replace("&amp;", "&");

    response = Jsoup.connect(loginAction)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .method(Method.POST)
        .followRedirects(false)
        .header("Accept", "*/*")
        .header("user-agent", USER_AGENT)
        .data("username", username)
        .data("password", password)
        .cookies(response.cookies())
        .execute();



    if (response.statusCode() != 302){
      throw new IOException("Could not login to PW. Probably bad username/password. Check your config");
    }

    List<NameValuePair> params = URLEncodedUtils.parse(response.header("Location"), Charset.forName("UTF-8"));

    Optional<NameValuePair> code = params.stream().filter(p-> p.getName().equals("code")).findAny();


    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", "external-login");
    body.add("grant_type", "authorization_code");
    body.add("code", code.get().getValue());
    body.add(REDIRECT_URI, DUMMY_REDIRECT);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Accept", "application/json");
    headers.add("User-agent", USER_AGENT);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<Auth> res = restTemplate.exchange("https://login.planetwatch.io/auth/realms/Planetwatch/protocol/openid-connect/token", HttpMethod.POST, entity, Auth.class);

    log.info("Login to PW: OK");
    return res.getBody();
  }


}
