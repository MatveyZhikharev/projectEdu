import apiClient from './index'

export interface BlockResponse {
  id: number
  title: string
  sortOrder: number
  isAvailable: boolean
  testId: number | null
}

export const blocksApi = {
  getAllAvailableBlocks() {
    return apiClient.get<BlockResponse[]>('/blocks')
  },

  getBlockImageUrl(blockId: number) {
    return `/api/blocks/${blockId}/image`
  },

  getBlockVideoUrl(blockId: number) {
    return `/api/blocks/${blockId}/video`
  },
}
