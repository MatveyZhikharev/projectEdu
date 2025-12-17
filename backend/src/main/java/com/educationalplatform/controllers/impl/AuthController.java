package com.educationalplatform.controllers.impl;

import com.educationalplatform.controllers.AuthOperations;
import com.educationalplatform.domain.dto.response.UserResponse;
import com.educationalplatform.domain.model.User;
import com.educationalplatform.service.AuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthOperations {

  @Value("${vk.redirect-uri-after}")
  private String redirectUri;

  private final AuthService authService;

  @Override
  public ResponseEntity<Map<String, String>> getVkAuthUrl(String deviceId) {
    return ResponseEntity.ok(
        Map.of("url", authService.generateVkAuthUrl(deviceId))
    );
  }

  @Override
  public ResponseEntity<Void> callback(String code, String state, String deviceId) {
    ResponseCookie cookie = authService.handleCallback(code, state, deviceId);
    System.out.println(cookie + "GOOOL");
    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .header(HttpHeaders.LOCATION, redirectUri)
        .build();
  }

  @Override
  public ResponseEntity<Map<String, Boolean>> getUserAuthStatus(User user) {
    Map<String, Boolean> result = Map.of("status", Boolean.TRUE);
    if (user == null) {
      result = Map.of("status", Boolean.FALSE);
    }
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<UserResponse> getUser(User user) {
    UserResponse result = new UserResponse();
    if (user != null) {
      result.setEmail(user.getEmail());
      result.setFirstName(user.getFirstName());
      result.setLastName(user.getLastName());
      result.setVkId(user.getVkId());
    }
    return ResponseEntity.ok(result);
  }
}
