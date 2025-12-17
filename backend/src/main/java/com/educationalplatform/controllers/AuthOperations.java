package com.educationalplatform.controllers;

import com.educationalplatform.domain.dto.response.UserResponse;
import com.educationalplatform.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Authentication operations", description = "Авторизация и регистрация")
@RequestMapping("/api/auth")
public interface AuthOperations {
  @Operation(summary = "Получить ссылку для авторизации через VK")
  @ApiResponse(responseCode = "200", description = "Ссылка успешно сформирована")
  @GetMapping("/vkUrl")
  ResponseEntity<Map<String, String>> getVkAuthUrl(
      @RequestParam(value = "device_id", required = false) String deviceId);

  @Operation(summary = "Обработка ответа от ВК")
  @ApiResponse(responseCode = "200", description = "Пользователь вошел")
  @GetMapping("/vkCallback")
  ResponseEntity<Void> callback(
      @RequestParam("code") String code,
      @RequestParam("state") String state,
      @RequestParam("device_id") String deviceId);

  @Operation(summary = "Зарегался ли пользователь?")
  @GetMapping("/status")
  ResponseEntity<Map<String, Boolean>> getUserAuthStatus(@AuthenticationPrincipal User user);

  @Operation(summary = "Получить пользователя")
  @GetMapping("/user")
  ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal User user);
}
