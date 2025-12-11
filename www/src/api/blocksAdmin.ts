import apiClient from './index'
import type { BlockResponse } from './blocks'

export interface BlockAddRequest {
  title: string
}

export interface BlockUpdateRequest {
  blockId: number
  title: string
}

export const blocksAdminApi = {
  /**
   * Create a new block
   */
  createBlock(data: BlockAddRequest) {
    return apiClient.post<BlockResponse>('/admin/blocks', data)
  },

  /**
   * Get all blocks (including unpublished)
   */
  getAllBlocks() {
    return apiClient.get<BlockResponse[]>('/admin/blocks')
  },

  /**
   * Toggle block publish status
   */
  toggleBlockStatus(blockId: number) {
    return apiClient.patch<void>(`/admin/blocks/${blockId}/status`)
  },

  /**
   * Swap two blocks order
   */
  swapBlocks(firstBlockId: number, secondBlockId: number) {
    return apiClient.put<void>(`/admin/blocks/${firstBlockId}/swap/${secondBlockId}`)
  },

  /**
   * Update block image
   */
  updateBlockImage(blockId: number, image: File) {
    const formData = new FormData()
    formData.append('image', image)
    return apiClient.put<void>(`/admin/blocks/${blockId}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * Update block video
   */
  updateBlockVideo(blockId: number, video: File) {
    const formData = new FormData()
    formData.append('video', video)
    return apiClient.put<void>(`/admin/blocks/${blockId}/video`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * Update block text data
   */
  updateBlock(data: BlockUpdateRequest) {
    return apiClient.put<BlockResponse>('/admin/blocks', data)
  },

  /**
   * Delete a block
   */
  deleteBlock(blockId: number) {
    return apiClient.delete<void>(`/admin/blocks/${blockId}`)
  },
}
