package com.educationalplatform.service;

import com.educationalplatform.domain.dto.response.ChunkResponse;
import com.educationalplatform.domain.dto.response.VideoFileResponse;
import com.educationalplatform.domain.dto.response.VideoInfoResponse;
import com.educationalplatform.domain.enums.VideoFormat;
import com.educationalplatform.domain.enums.VideoStatus;
import com.educationalplatform.domain.model.Block;
import com.educationalplatform.domain.model.Video;
import com.educationalplatform.exceptions.BadRequestException;
import com.educationalplatform.exceptions.BlockNotFoundException;
import com.educationalplatform.repository.BlockRepository;
import com.educationalplatform.repository.VideoRepository;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VideoService {

  private static final int DEFAULT_CHUNK_SIZE = 1024 * 1024;

  private final BlockRepository blockRepository;
  private final VideoRepository videoRepository;

  private final FileStorageService fileStorageService;
  private final VideoEncryptionService encryptionService;

  @Transactional
  public void updateVideo(Long blockId, MultipartFile videoFile, String description) {
    Block block = blockRepository.findById(blockId)
        .orElseThrow(BlockNotFoundException::new);
    if (block.getTest() != null) {
      throw new BadRequestException("Either a test or a video can be linked to the block");
    }
    if (block.getVideo() != null) {
      fileStorageService.deleteVideo(block.getVideo().getId());
    }
    VideoFormat videoFormat = detectVideoFormat(videoFile);
    long fileSize = videoFile.getSize();
    int totalChunks = (int) Math.ceil((double) fileSize / DEFAULT_CHUNK_SIZE);

    String encryptionKey = generateAesKey();

    Video video = Video.builder()
        .description(description)
        .fileSize(fileSize)
        .mimeType(videoFile.getContentType() != null ? videoFile.getContentType() : "video/mp4")
        .format(videoFormat)
        .status(VideoStatus.READY)
        .chunkSize(DEFAULT_CHUNK_SIZE)
        .totalChunks(totalChunks)
        .encryptionKey(encryptionKey)
        .durationSeconds(0)
        .build();

    fileStorageService.uploadVideo(blockId, videoFile);

    video = videoRepository.save(video);
    block.setVideo(video);
    blockRepository.save(block);
  }

  @Transactional(readOnly = true)
  public VideoInfoResponse getVideoInfo(Long blockId) {
    Video video = getVideoByBlockId(blockId);
    return VideoInfoResponse.fromEntity(video);
  }

  @Transactional(readOnly = true)
  public String getVideoContentType(Long blockId) {
    Video video = getVideoByBlockId(blockId);
    return video.getMimeType();
  }

  @Transactional(readOnly = true)
  public VideoFileResponse getVideoStream(Long blockId) {
    Video video = getVideoByBlockId(blockId);
    InputStream videoStream = fileStorageService.getVideo(video.getId());
    return VideoFileResponse.builder()
        .video(videoStream)
        .mediaType(MediaType.parseMediaType(video.getMimeType()))
        .build();
  }

  @Transactional(readOnly = true)
  public ChunkResponse getChunk(Long blockId, Integer chunkIndex) {
    Video video = getVideoByBlockId(blockId);

    if (chunkIndex < 0 || chunkIndex >= video.getTotalChunks()) {
      throw new BadRequestException("Индекс чанка " + chunkIndex + " не найден");
    }

    try {
      long chunkSize = video.getChunkSize();
      long offset = (long) chunkIndex * chunkSize;
      long length = Math.min(chunkSize, video.getFileSize() - offset);

      try (InputStream stream = fileStorageService.getVideo(video.getId(), offset, length)) {

        byte[] chunkData = stream.readAllBytes();
        byte[] encryptedData = encryptionService.encrypt(chunkData, video.getEncryptionKey());

        return ChunkResponse.builder()
            .chunkIndex(chunkIndex)
            .encryptedData(encryptedData)
            .build();
      }
    } catch (Exception e) {
      throw new BadRequestException("Ошибка обработки видео чанка");
    }
  }

  public Video getVideoByBlockId(Long blockId) {
    Block block = blockRepository.findById(blockId)
        .orElseThrow(BlockNotFoundException::new);
    if (block.getVideo() == null) {
      throw new BadRequestException("Block has no video");
    }
    return block.getVideo();
  }

  public static VideoFormat detectVideoFormat(MultipartFile video) {
    if (video == null) {
      throw new IllegalArgumentException("Video file is null");
    }

    String mimeType = video.getContentType();
    Optional<VideoFormat> fromMime = VideoFormat.fromMimeType(mimeType);
    if (fromMime.isPresent()) {
      return fromMime.get();
    }

    String originalName = video.getOriginalFilename();
    if (originalName == null || originalName.isBlank()) {
      throw new IllegalArgumentException("Cannot determine video format: filename is empty");
    }

    String ext = originalName.substring(originalName.lastIndexOf('.') + 1);

    Optional<VideoFormat> fromExt = VideoFormat.fromExtension(ext);
    if (fromExt.isPresent()) {
      return fromExt.get();
    }

    throw new IllegalArgumentException(
        "Unsupported video format: mimeType=" + mimeType + ", extension=" + ext
    );
  }

  private String generateAesKey() {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      SecretKey secretKey = keyGen.generateKey();
      return Base64.getUrlEncoder().encodeToString(secretKey.getEncoded());
    } catch (Exception e) {
      throw new RuntimeException("Error generating encryption key", e);
    }
  }
}
