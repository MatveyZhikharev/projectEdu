package com.educationalplatform.domain.enums;

import java.util.Arrays;
import java.util.Optional;

public enum VideoFormat {
  MP4("mp4", "video/mp4"),
  WEBM("webm", "video/webm"),
  MOV("mov", "video/quicktime"),
  AVI("avi", "video/x-msvideo");

  private final String extension;
  private final String mimeType;

  VideoFormat(final String extension, final String mimeType) {
    this.extension = extension;
    this.mimeType = mimeType;
  }

  public String getExtension() {
    return extension;
  }

  public String getMimeType() {
    return mimeType;
  }

  public static Optional<VideoFormat> fromExtension(final String extension) {
    if (extension == null || extension.isBlank()) {
      return Optional.empty();
    }

    final String cleanExtension =
        extension.startsWith(".") ? extension.substring(1).toLowerCase() : extension.toLowerCase();

    return Arrays.stream(VideoFormat.values())
        .filter(format -> format.extension.equals(cleanExtension))
        .findFirst();
  }

  public static Optional<VideoFormat> fromMimeType(final String mimeType) {
    if (mimeType == null || mimeType.isBlank()) {
      return Optional.empty();
    }

    return Arrays.stream(VideoFormat.values())
        .filter(format -> format.mimeType.equalsIgnoreCase(mimeType))
        .findFirst();
  }

  public static boolean isSupported(final String extension) {
    return fromExtension(extension).isPresent();
  }

  @Override
  public String toString() {
    return String.format("%s (extension: .%s, mimeType: %s)", name(), extension, mimeType);
  }
}