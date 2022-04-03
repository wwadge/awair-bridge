package com.pw.authhelper.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelperController {

  @GetMapping("/hello")
  public ResponseEntity<String> hello(Authentication authentication) {
    final String body = "Hello " + authentication.getName();
    return ResponseEntity.ok(body);
  }
}