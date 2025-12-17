package com.educationalplatform.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.educationalplatform.domain.enums.VideoFormat;
import com.educationalplatform.domain.enums.VideoStatus;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Enumerated(EnumType.STRING)
  @Column(name = "format", nullable = false, length = 20)
  private VideoFormat format;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private VideoStatus status = VideoStatus.PENDING;

  @Column(name = "mime_type", nullable = false, length = 50)
  private String mimeType;

  @Column(name = "encryption_key", nullable = false, length = 500)
  private String encryptionKey;

  @Column(name = "total_chunks", nullable = false)
  private Integer totalChunks;

  @Column(name = "chunk_size", nullable = false)
  private Integer chunkSize;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    final LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;

    if (status == null) {
      status = VideoStatus.PENDING;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public boolean isReadyForStreaming() {
    return status == VideoStatus.READY;
  }

  public String getFormattedDuration() {
    if (durationSeconds == null) {
      return "00:00";
    }

    final int hours = durationSeconds / 3600;
    final int minutes = (durationSeconds % 3600) / 60;
    final int seconds = durationSeconds % 60;

    if (hours > 0) {
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    return String.format("%02d:%02d", minutes, seconds);
  }

  public String getFormattedFileSize() {
    if (fileSize == null) {
      return "0 B";
    }

    final long kb = 1024;
    final long mb = kb * 1024;
    final long gb = mb * 1024;

    if (fileSize >= gb) {
      return String.format("%.2f GB", fileSize / (double) gb);
    } else if (fileSize >= mb) {
      return String.format("%.2f MB", fileSize / (double) mb);
    } else if (fileSize >= kb) {
      return String.format("%.2f KB", fileSize / (double) kb);
    }
    return fileSize + " B";
  }
}