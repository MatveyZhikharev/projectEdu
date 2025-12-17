package com.educationalplatform.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChunkResponse {

  @JsonProperty("chunkIndex")
  private Integer chunkIndex;

  @JsonProperty("encryptedData")
  private byte[] encryptedData;

  @JsonProperty("iv")
  private byte[] initializationVector;

  @JsonProperty("isLastChunk")
  private Boolean isLastChunk;
}