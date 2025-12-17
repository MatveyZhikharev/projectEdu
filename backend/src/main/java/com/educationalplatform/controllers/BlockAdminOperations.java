package com.educationalplatform.controllers;

import com.educationalplatform.domain.dto.request.BlockAddRequest;
import com.educationalplatform.domain.dto.request.BlockUpdateRequest;
import com.educationalplatform.domain.dto.response.BlockResponse;
import com.educationalplatform.domain.dto.response.VideoInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Blocks", description = "API для управления блоками")
@RequestMapping("/api/admin/blocks")
public interface BlockAdminOperations {

  @Operation(summary = "Создать блок")
  @PostMapping
  ResponseEntity<BlockResponse> add(@RequestBody BlockAddRequest request);

  @Operation(summary = "Опубликовать блок")
  @PatchMapping("/{blockId}/status")
  ResponseEntity<Void> editBlockStatus(@PathVariable Long blockId);

  @Operation(summary = "Поменять местами два блока")
  @PutMapping("/{firstBlockId}/swap/{secondBlockId}")
  ResponseEntity<Void> swapBlocks(
      @PathVariable Long firstBlockId,
      @PathVariable Long secondBlockId);

  @Operation(summary = "Обновить превью блока (заменить)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Превью успешно загружено",
          content = {@Content(mediaType = "multipart/form-data")}),
      @ApiResponse(responseCode = "400", description = "Ошибка при загрузке превью")
  })
  @PutMapping(value = "/{blockId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<Void> updatePhoto(
      @PathVariable Long blockId,
      @RequestPart("image") MultipartFile image);

  @Operation(summary = "Загрузить новое видео")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Видео успешно загружено",
          content = {@Content(mediaType = "multipart/form-data",
              schema = @Schema(implementation = VideoInfoResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Ошибка при загрузке видео")
  })
  @PostMapping(value = "/{blockId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<VideoInfoResponse> uploadVideo(
      @PathVariable Long blockId,
      @Parameter(description = "Файл видео") @RequestParam("file") MultipartFile file,
      @Parameter(description = "Описание видео") @RequestParam(value = "description", required = false) String description);

  @Operation(summary = "Стриминг видео для скачивания видео")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Видео отдано для воспроизведения"),
      @ApiResponse(responseCode = "404", description = "Видео не найдено")
  })
  @GetMapping("/{blockId}/video")
  ResponseEntity<Resource> streamVideo(@PathVariable("blockId") Long blockId);

  @Operation(summary = "Обновить текстовые данные блока")
  @PutMapping
  ResponseEntity<BlockResponse> updateBlock(@RequestBody BlockUpdateRequest request);

  @Operation(summary = "Получить все блоки")
  @GetMapping
  ResponseEntity<List<BlockResponse>> getAllBlocks();

  @Operation(summary = "Удалить блок")
  @DeleteMapping(value = "/{blockId}")
  ResponseEntity<Void> deleteBlock(@PathVariable Long blockId);
}
