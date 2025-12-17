package com.educationalplatform.controllers;

import com.educationalplatform.domain.dto.response.ChunkResponse;
import com.educationalplatform.domain.dto.response.VideoInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Video API", description = "Управление видео и потоковой передачей")
@RequestMapping("/api/video")
public interface VideoOperation {
  @Operation(summary = "Получить информацию о видео по ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Информация о видео получена",
          content = {@Content(mediaType = "application/json",
              schema = @Schema(implementation = VideoInfoResponse.class))}),
      @ApiResponse(responseCode = "404", description = "Видео не найдено")
  })
  @GetMapping("/{blockId}")
  ResponseEntity<VideoInfoResponse> getVideoInfo(@PathVariable("blockId") Long blockId);

  @Operation(summary = "Получить зашифрованный чанк видео")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Чанк видео получен",
          content = {@Content(mediaType = "application/json",
              schema = @Schema(implementation = ChunkResponse.class))}),
      @ApiResponse(responseCode = "404", description = "Видео или чанк не найден")
  })
  @GetMapping("/{blockId}/stream/{chunkIndex}")
  ResponseEntity<ChunkResponse> getVideoChunk(
      @PathVariable("blockId") Long blockId,
      @PathVariable("chunkIndex") Integer chunkIndex);

  @Operation(summary = "Получить MIME тип видео")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "MIME тип видео получен",
          content = {@Content(mediaType = "text/plain")}),
      @ApiResponse(responseCode = "404", description = "Видео не найдено")
  })
  @GetMapping("/{blockId}/content-type")
  ResponseEntity<String> getVideoContentType(@PathVariable("blockId") Long blockId);
}
