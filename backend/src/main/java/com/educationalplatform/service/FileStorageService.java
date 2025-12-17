package com.educationalplatform.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

  private final MinioClient minioClient;

  @Value("${minio.bucket-name}")
  private String bucketName;

  public String uploadFile(MultipartFile file) {
    try {
      createBucketIfNotExists();

      String fileExtension = getFileExtension(file.getOriginalFilename());
      String fileName = UUID.randomUUID() + fileExtension;

      minioClient.putObject(
        PutObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .stream(file.getInputStream(), file.getSize(), -1)
          .contentType(file.getContentType())
          .build()
      );

      log.info("File uploaded successfully: {}", fileName);
      return fileName;

    } catch (Exception e) {
      log.error("Error uploading file to MinIO", e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  public InputStream getFile(String fileName) {
    try {
      return minioClient.getObject(
        GetObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );
    } catch (Exception e) {
      log.error("Error getting file from MinIO: {}", fileName, e);
      throw new RuntimeException("Failed to get file", e);
    }
  }

  public void deleteFile(String fileName) {
    try {
      minioClient.removeObject(
        RemoveObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );
      log.info("File deleted successfully: {}", fileName);
    } catch (Exception e) {
      log.error("Error deleting file from MinIO: {}", fileName, e);
      throw new RuntimeException("Failed to delete file", e);
    }
  }

  public String uploadBlockImage(Long blockId, MultipartFile image) {
    return uploadFileToFolder("blocks", blockId.toString(), "image", image);
  }

  public InputStream getBlockImage(Long blockId) {
    return getFileFromFolder("blocks", blockId.toString(), "image");
  }

  public void deleteBlockImage(Long blockId) {
    deleteFileFromFolder("blocks", blockId.toString(), "image");
  }

  public boolean hasBlockImage(Long blockId) {
    return hasFileInFolder("blocks", blockId.toString(), "image");
  }

  public String uploadVideo(Long videoId, MultipartFile video) {
    return uploadFileToFolder("videos", videoId.toString(), "video", video);
  }

  public InputStream getVideo(Long videoId) {
    return getFileFromFolder("videos", videoId.toString(), "video");
  }

  public InputStream getVideo(Long videoId, long offset, long length) {
    return getFileFromFolder("videos", videoId.toString(), "video", offset, length);
  }

  public void deleteVideo(Long videoId) {
    deleteFileFromFolder("videos", videoId.toString(), "video");
  }

  public boolean hasVideo(Long videoId) {
    return hasFileInFolder("videos", videoId.toString(), "video");
  }

  private String uploadFileToFolder(String folder, String entityId, String fileType, MultipartFile file) {
    try {
      createBucketIfNotExists();

      String fileExtension = getFileExtension(file.getOriginalFilename());
      String fileName = folder + "/" + entityId + "/" + fileType + fileExtension;

      minioClient.putObject(
        PutObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .stream(file.getInputStream(), file.getSize(), -1)
          .contentType(file.getContentType())
          .build()
      );

      log.info("{} uploaded: {}/{}, fileName={}", fileType, folder, entityId, fileName);
      return fileName;

    } catch (Exception e) {
      log.error("Error uploading {} for {}/{}", fileType, folder, entityId, e);
      throw new RuntimeException("Failed to upload " + fileType, e);
    }
  }

  private InputStream getFileFromFolder(String folder, String entityId, String fileType) {
    String fileName = resolveFileName(folder, entityId, fileType);

    try {
      return minioClient.getObject(
          GetObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .build()
      );
    } catch (Exception e) {
      log.error("Error getting {} for {}/{}", fileType, folder, entityId, e);
      throw new RuntimeException("Failed to get " + fileType, e);
    }
  }

  private InputStream getFileFromFolder(String folder, String entityId, String fileType,
      long offset, long length) {
    String fileName = resolveFileName(folder, entityId, fileType);

    try {
      return minioClient.getObject(
          GetObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .offset(offset)
              .length(length)
              .build()
      );
    } catch (Exception e) {
      log.error("Error getting {} (offset={}, length={}) for {}/{}",
          fileType, offset, length, folder, entityId, e);
      throw new RuntimeException("Failed to get " + fileType, e);
    }
  }

  private String resolveFileName(String folder, String entityId, String fileType) {
    String fileNamePattern = folder + "/" + entityId + "/" + fileType;
    String fileName = null;
    try {
      fileName = findFileByPattern(fileNamePattern);
    } catch (Exception e) {
      //pass
    }

    if (fileName == null) {
      throw new RuntimeException(fileType + " not found for " + folder + ": " + entityId);
    }
    return fileName;
  }

  private void deleteFileFromFolder(String folder, String entityId, String fileType) {
    try {
      String fileNamePattern = folder + "/" + entityId + "/" + fileType;
      String fileName = findFileByPattern(fileNamePattern);

      if (fileName != null) {
        minioClient.removeObject(
          RemoveObjectArgs.builder()
            .bucket(bucketName)
            .object(fileName)
            .build()
        );
        log.info("{} deleted: {}/{}, fileName={}", fileType, folder, entityId, fileName);
      }
    } catch (Exception e) {
      log.error("Error deleting {} for {}/{}", fileType, folder, entityId, e);
      throw new RuntimeException("Failed to delete " + fileType, e);
    }
  }

  private boolean hasFileInFolder(String folder, String entityId, String fileType) {
    try {
      String fileNamePattern = folder + "/" + entityId + "/" + fileType;
      return findFileByPattern(fileNamePattern) != null;
    } catch (Exception e) {
      log.error("Error checking {} for {}/{}", fileType, folder, entityId, e);
      return false;
    }
  }

  private String findFileByPattern(String pattern) throws Exception {
    Iterable<Result<Item>> results = minioClient.listObjects(
      ListObjectsArgs.builder()
        .bucket(bucketName)
        .prefix(pattern)
        .build()
    );

    for (Result<Item> result : results) {
      Item item = result.get();
      if (item.objectName().startsWith(pattern)) {
        return item.objectName();
      }
    }
    return null;
  }

  private void createBucketIfNotExists() throws Exception {
    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    if (!found) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
      log.info("Bucket created: {}", bucketName);
    }
  }

  private String getFileExtension(String fileName) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(fileName.lastIndexOf("."));
    }
    return "";
  }
}
