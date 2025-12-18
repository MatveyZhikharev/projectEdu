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
  createBlock(data: BlockAddRequest) {
    return apiClient.post<BlockResponse>('/admin/blocks', data)
  },

  getAllBlocks() {
    return apiClient.get<BlockResponse[]>('/admin/blocks')
  },

  toggleBlockStatus(blockId: number) {
    return apiClient.patch<void>(`/admin/blocks/${blockId}/status`)
  },

  swapBlocks(firstBlockId: number, secondBlockId: number) {
    return apiClient.put<void>(`/admin/blocks/${firstBlockId}/swap/${secondBlockId}`)
  },

  updateBlockImage(blockId: number, image: File) {
    const formData = new FormData()
    formData.append('image', image)
    return apiClient.put<void>(`/admin/blocks/${blockId}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  updateBlockVideo(blockId: number, video: File) {
    const formData = new FormData()
    formData.append('video', video)
    return apiClient.put<void>(`/admin/blocks/${blockId}/video`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  updateBlock(data: BlockUpdateRequest) {
    return apiClient.put<BlockResponse>('/admin/blocks', data)
  },

  deleteBlock(blockId: number) {
    return apiClient.delete<void>(`/admin/blocks/${blockId}`)
  },
}
