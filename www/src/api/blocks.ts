import apiClient from './index'

export interface BlockResponse {
  id: number
  title: string
  sortOrder: number
  isAvailable: boolean
  testId: number | null
}

export const blocksApi = {
  /**
   * Get all published blocks
   */
  getAllAvailableBlocks() {
    return apiClient.get<BlockResponse[]>('/blocks')
  },

  /**
   * Get block image URL
   */
  getBlockImageUrl(blockId: number) {
    return `/api/blocks/${blockId}/image`
  },
}
