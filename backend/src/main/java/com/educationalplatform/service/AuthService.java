package com.educationalplatform.service;

import com.educationalplatform.domain.model.User;
import com.educationalplatform.domain.model.VkAuthState;
import com.educationalplatform.exceptions.VkAuthException;
import com.educationalplatform.repository.UserRepository;
import com.educationalplatform.repository.VkAuthStateRepository;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  static final String AUTH_URL = "https://id.vk.ru/authorize";
  static final String TOKEN_URL = "https://id.vk.ru/oauth2/auth";
  static final String GET_USER_INFO_URL = "https://id.vk.ru/oauth2/user_info";

  private static final String ALLOWED_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int RANDOM_STRING_LENGTH = 32;
  private static final SecureRandom secureRandom = new SecureRandom();

  @Value("${jwt.name}")
  private String jwtName;

  @Value("${jwt.expiration}")
  private Long keyExpiration;

  @Value("${vk.app-id}")
  private String appId;

  @Value("${vk.secret}")
  private String appSecret;

  @Value("${vk.redirect-uri}")
  private String redirectUri;

  private final VkAuthStateRepository vkAuthStateRepository;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final RestTemplate restTemplate;

  public String generateVkAuthUrl(String deviceId) {
    String state = generateRandomString(RANDOM_STRING_LENGTH);
    String codeVerifier = generateCodeVerifier();
    String codeChallenge = generateCodeChallenge(codeVerifier);

    VkAuthState vkAuthState = VkAuthState.builder()
        .state(state)
        .codeVerifier(codeVerifier)
        .build();
    vkAuthStateRepository.save(vkAuthState);

    String uri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(AUTH_URL)
        .queryParam("response_type", "code")
        .queryParam("client_id", appId)
        .queryParam("redirect_uri", uri)
        .queryParam("state", state)
        .queryParam("scope", "email")
        .queryParam("code_challenge", codeChallenge)
        .queryParam("code_challenge_method", "S256");

    if (deviceId != null && !deviceId.isBlank()) {
      uriBuilder.queryParam("device_id", deviceId);
    }

    String authUrl = uriBuilder.build(true).toUriString();
    log.info("[VK AUTH] Generated VK authorization URL: {}", authUrl);
    return authUrl;
  }

  @Transactional
  public ResponseCookie handleCallback(String code, String state, String deviceId) {
    log.info("[VK AUTH] Received code from VK: {}, state: {}, deviceId: {}", code, state, deviceId);

    VkAuthState vkAuthState = vkAuthStateRepository.findByState(state)
        .orElseThrow(() -> new VkAuthException("State not found or expired"));

    String codeVerifier = vkAuthState.getCodeVerifier();
    vkAuthStateRepository.deleteByState(state);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> payload = new java.util.HashMap<>(Map.of(
        "grant_type", "authorization_code",
        "code_verifier", codeVerifier,
        "redirect_uri", redirectUri,
        "code", code,
        "client_id", appId,
        "client_secret", appSecret
    ));
    if (deviceId != null && !deviceId.isBlank()) {
      payload.put("device_id", deviceId);
    }

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    Map<String, Object> tokenResponse = restTemplate.postForObject(TOKEN_URL, request, Map.class);
    if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
      throw new VkAuthException("Invalid token response");
    }

    return processTokenResponse(tokenResponse);
  }

  private ResponseCookie processTokenResponse(Map<String, Object> tokenResponse) {
    Long vkUserId = ((Number) tokenResponse.get("user_id")).longValue();
    String vkToken = (String) tokenResponse.get("access_token");

    return userRepository.findByVkId(vkUserId)
        .map(this::getJwtCookie)
        .orElseGet(() -> registerUser(vkToken));
  }

  private ResponseCookie registerUser(String vkToken) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("access_token", vkToken);
    formData.add("client_id", appId);

    Map<String, Object> response = restTemplate.postForObject(GET_USER_INFO_URL, formData, Map.class);

    Map<String, Object> userInfo = (Map<String, Object>) response.get("user");
    if (userInfo == null || userInfo.isEmpty()) {
      throw new VkAuthException("Empty user info response");
    }
    User user = createUserFromVkMap(userInfo);
    userRepository.save(user);
    return getJwtCookie(user);
  }

  private User createUserFromVkMap(Map<String, Object> userData) {
    Long id = Long.parseLong((String) userData.get("user_id"));
    String firstName = (String) userData.get("first_name");
    String lastName = (String) userData.get("last_name");
    String email = (String) userData.get("email");

    return User.builder()
        .vkId(id)
        .firstName(firstName != null ? firstName : "")
        .lastName(lastName != null ? lastName : "")
        .email(email != null ? email : "")
        .build();
  }

  private ResponseCookie getJwtCookie(User user) {
    String jwt = jwtService.generateToken(user);
    return ResponseCookie.from(jwtName, jwt)
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofSeconds(keyExpiration / 1000))
        .build();
  }

  private String generateCodeVerifier() {
    int length = 43 + secureRandom.nextInt(86);
    return generateRandomString(length);
  }

  private String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALLOWED_CHARS.charAt(secureRandom.nextInt(ALLOWED_CHARS.length())));
    }
    return sb.toString();
  }

  private String generateCodeChallenge(String codeVerifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not supported", e);
    }
  }
}
