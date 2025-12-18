package com.educationalplatform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class VideoEncryptionService {

  private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final int IV_LENGTH = 16;

  private final SecureRandom secureRandom = new SecureRandom();

  public byte[] encrypt(byte[] data, String base64Key, byte[] initializationVector) {
    try {
      byte[] keyBytes = Base64.getUrlDecoder().decode(base64Key);

      SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
      IvParameterSpec ivSpec = new IvParameterSpec(initializationVector);

      Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
      return cipher.doFinal(data);
    } catch (Exception e) {
      log.error("Ошибка при шифровании видео чанка", e);
      throw new RuntimeException("Ошибка при шифровании данных", e);
    }
  }

  public byte[] encrypt(byte[] data, String base64Key) {
    byte[] iv = generateIv();
    return encrypt(data, base64Key, iv);
  }

  public byte[] generateIv() {
    byte[] iv = new byte[IV_LENGTH];
    secureRandom.nextBytes(iv);
    return iv;
  }
}
