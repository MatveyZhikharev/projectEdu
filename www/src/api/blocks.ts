import apiClient from './index'

export interface BlockResponse {
  id: number
  title: string
  sortOrder: number
  isAvailable: boolean
  testId: number | null
}

export interface VideoInfoResponse {
  id: number
  description: string | null
  fileSize: number
  formattedFileSize: string
  durationSeconds: number | null
  formattedDuration: string
  format: string
  status: string
  mimeType: string
  totalChunks: number
  chunkSize: number
  createdAt: string
  updatedAt: string
}

export interface ChunkResponse {
  chunkIndex: number
  encryptedData: string // base64 encoded
  iv: string // base64 encoded (initializationVector)
  isLastChunk: boolean
}

export const blocksApi = {
  getAllAvailableBlocks() {
    return apiClient.get<BlockResponse[]>('/blocks')
  },

  getBlockImageUrl(blockId: number) {
    return `/api/blocks/${blockId}/image`
  },

  // Get video info for a block (includes description, duration, chunk info, etc.)
  getBlockVideoInfo(blockId: number) {
    return apiClient.get<VideoInfoResponse>(`/video/${blockId}`)
  },

  // Get a specific video chunk for streaming
  getBlockVideoChunk(blockId: number, chunkIndex: number) {
    return apiClient.get<ChunkResponse>(`/video/${blockId}/stream/${chunkIndex}`)
  },

  // Get video content type/MIME type
  getBlockVideoContentType(blockId: number) {
    return apiClient.get<string>(`/video/${blockId}/content-type`)
  },

  // Direct video stream URL (for admin download)
  getBlockVideoStreamUrl(blockId: number) {
    return `/api/admin/blocks/${blockId}/video`
  },
}
