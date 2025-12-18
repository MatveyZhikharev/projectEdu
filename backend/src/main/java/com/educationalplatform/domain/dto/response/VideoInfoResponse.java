package com.educationalplatform.domain.dto.response;


import com.educationalplatform.domain.enums.VideoFormat;
import com.educationalplatform.domain.enums.VideoStatus;
import com.educationalplatform.domain.model.Video;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoInfoResponse {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("description")
  private String description;

  @JsonProperty("fileSize")
  private Long fileSize;

  @JsonProperty("formattedFileSize")
  private String formattedFileSize;

  @JsonProperty("durationSeconds")
  private Integer durationSeconds;

  @JsonProperty("formattedDuration")
  private String formattedDuration;

  @JsonProperty("format")
  private VideoFormat format;

  @JsonProperty("status")
  private VideoStatus status;

  @JsonProperty("mimeType")
  private String mimeType;

  @JsonProperty("totalChunks")
  private Integer totalChunks;

  @JsonProperty("chunkSize")
  private Integer chunkSize;

  @JsonProperty("createdAt")
  private LocalDateTime createdAt;

  @JsonProperty("updatedAt")
  private LocalDateTime updatedAt;

  public static VideoInfoResponse fromEntity(final Video video) {
    return VideoInfoResponse.builder()
        .id(video.getId())
        .description(video.getDescription())
        .fileSize(video.getFileSize())
        .formattedFileSize(video.getFormattedFileSize())
        .durationSeconds(video.getDurationSeconds())
        .formattedDuration(video.getFormattedDuration())
        .format(video.getFormat())
        .status(video.getStatus())
        .mimeType(video.getMimeType())
        .totalChunks(video.getTotalChunks())
        .chunkSize(video.getChunkSize())
        .createdAt(video.getCreatedAt())
        .updatedAt(video.getUpdatedAt())
        .build();
  }
}