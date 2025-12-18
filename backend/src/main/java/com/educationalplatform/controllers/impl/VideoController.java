package com.educationalplatform.controllers.impl;

import com.educationalplatform.controllers.VideoOperation;
import com.educationalplatform.domain.dto.response.ChunkResponse;
import com.educationalplatform.domain.dto.response.VideoInfoResponse;
import com.educationalplatform.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VideoController implements VideoOperation {

  private final VideoService videoService;

  @Override
  public ResponseEntity<VideoInfoResponse> getVideoInfo(Long blockId) {
    return ResponseEntity.ok(videoService.getVideoInfo(blockId));
  }

  @Override
  public ResponseEntity<ChunkResponse> getVideoChunk(Long blockId, Integer chunkIndex) {
    return ResponseEntity.ok(videoService.getChunk(blockId, chunkIndex));
  }

  @Override
  public ResponseEntity<String> getVideoContentType(Long blockId) {
    return ResponseEntity.ok(videoService.getVideoContentType(blockId));
  }
}
