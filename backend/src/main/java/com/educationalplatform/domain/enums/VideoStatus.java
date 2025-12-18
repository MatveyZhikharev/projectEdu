package com.educationalplatform.domain.enums;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VideoStatus {
  PENDING("Ожидает обработки"),
  PROCESSING("Обрабатывается"),
  READY("Готово к просмотру"),
  ERROR("Ошибка обработки"),
  DELETED("Удалено");

  private final String description;

  public static Optional<VideoStatus> fromString(final String status) {
    if (status == null || status.isBlank()) {
      return Optional.empty();
    }

    return Arrays.stream(VideoStatus.values())
        .filter(s -> s.name().equalsIgnoreCase(status))
        .findFirst();
  }

  public boolean isAvailableForStreaming() {
    return this == READY;
  }

  public boolean canBeModified() {
    return this == PENDING || this == ERROR;
  }

  public boolean isFinalState() {
    return this == READY || this == ERROR || this == DELETED;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", name(), description);
  }
}