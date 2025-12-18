package com.educationalplatform.service;

import com.educationalplatform.domain.dto.request.BlockAddRequest;
import com.educationalplatform.domain.dto.request.BlockUpdateRequest;
import com.educationalplatform.domain.dto.response.BlockResponse;
import com.educationalplatform.domain.model.Block;
import com.educationalplatform.exceptions.BadRequestException;
import com.educationalplatform.exceptions.BlockNotFoundException;
import com.educationalplatform.repository.BlockRepository;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BlockService {

  private final BlockRepository blockRepository;

  private final FileStorageService fileStorageService;

  @Transactional
  public BlockResponse add(BlockAddRequest blockAddRequest) {
    int sortOrder = blockRepository.findMaxSortOrder() + 1;
    Block block = Block.builder()
        .title(blockAddRequest.getTitle())
        .sortOrder(sortOrder)
        .build();
    blockRepository.save(block);
    return BlockResponse.fromEntity(block);
  }

  @Transactional
  public void editBlockStatus(Long blockId) {
    Block block = blockRepository.findById(blockId)
        .orElseThrow(BlockNotFoundException::new);
    if (block.getIsAvailable()) {
      block.setIsAvailable(false);
    } else {
      boolean hasTest = block.getTest() != null;
      boolean hasVideo = block.getVideo() != null;
      if (!hasTest && !hasVideo) {
        throw new BadRequestException("Cannot publish block without attached test or video");
      }
      block.setIsAvailable(true);
    }
    blockRepository.save(block);
  }

  @Transactional
  public void swapBlocks(Long firstBlockId, Long secondBlockId) {
    if (firstBlockId.equals(secondBlockId)) throw new BadRequestException("Block ids must differ");
    Block firstBlock = blockRepository.findById(firstBlockId)
        .orElseThrow(BlockNotFoundException::new);
    Block secondBlock = blockRepository.findById(secondBlockId)
        .orElseThrow(BlockNotFoundException::new);
    Integer tmp = firstBlock.getSortOrder();
    firstBlock.setSortOrder(secondBlock.getSortOrder());
    secondBlock.setSortOrder(tmp);
    blockRepository.save(firstBlock);
    blockRepository.save(firstBlock);
  }

  @Transactional
  public void updatePhoto(Long blockId, MultipartFile image) {
    Block block = blockRepository.findById(blockId)
        .orElseThrow(BlockNotFoundException::new);
    if (fileStorageService.hasBlockImage(blockId)) {
      fileStorageService.deleteBlockImage(blockId);
    }
    fileStorageService.uploadBlockImage(blockId, image);
    block.setHasImage(true);
    blockRepository.save(block);
  }

  @Transactional
  public BlockResponse updateBlock(BlockUpdateRequest updateRequest) {
    Block block = blockRepository.findById(updateRequest.getBlockId())
        .orElseThrow(BlockNotFoundException::new);
    block.setTitle(updateRequest.getTitle());
    Block saved = blockRepository.save(block);
    return BlockResponse.fromEntity(saved);
  }

  public void deleteBlock(Long blockId) {
    Block block = blockRepository.findById(blockId)
        .orElseThrow(BlockNotFoundException::new);
    block.setIsAvailable(false);
    blockRepository.save(block);
    if (block.getHasImage()) {
      fileStorageService.deleteBlockImage(blockId);
    }
    if (block.getVideo() != null) {
      fileStorageService.deleteVideo(block.getVideo().getId());
    }
    blockRepository.delete(block);
  }

  @Transactional(readOnly = true)
  public List<BlockResponse> getAllBlocks() {
    List<Block> blocks = blockRepository.findAll();
    return blocks.stream().map(BlockResponse::fromEntity).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BlockResponse> getAllAvailableBlocks() {
    List<Block> blocks = blockRepository.findAllByIsAvailableIsTrue();
    return blocks.stream().map(BlockResponse::fromEntity).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public InputStream getBlockImage(Long blockId) {
    blockRepository.findById(blockId).orElseThrow(BlockNotFoundException::new);
    return fileStorageService.getBlockImage(blockId);
  }
}
