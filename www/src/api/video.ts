import apiClient from './index'

export interface VideoDTO {
  id: number
  title: string
  description: string | null
  status: string
  mimeType: string | null
  duration: number | null
  createdAt: string
}

export interface VideoInfoDTO {
  id: number
  title: string
  description: string | null
  totalChunks: number
  mimeType: string
  duration: number | null
}

export interface ChunkResponseDTO {
  chunkIndex: number
  totalChunks: number
  encryptedData: string
  iv: string
}

export const videoApi = {
  /**
   * Get all videos
   */
  getAllVideos() {
    return apiClient.get<VideoDTO[]>('/v1/videos')
  },

  /**
   * Get video info by ID
   */
  getVideoInfo(id: number) {
    return apiClient.get<VideoInfoDTO>(`/v1/videos/${id}`)
  },

  /**
   * Get encrypted video chunk
   */
  getVideoChunk(id: number, chunkIndex: number) {
    return apiClient.get<ChunkResponseDTO>(`/v1/videos/${id}/stream/${chunkIndex}`)
  },

  /**
   * Get video MIME type
   */
  getVideoContentType(id: number) {
    return apiClient.get<string>(`/v1/videos/${id}/content-type`)
  },

  /**
   * Get video stream URL (for direct playback)
   */
  getVideoStreamUrl(id: number) {
    return `/api/v1/videos/${id}/stream`
  },

  /**
   * Upload a new video
   */
  uploadVideo(file: File, title: string, description?: string) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('title', title)
    if (description) {
      formData.append('description', description)
    }
    return apiClient.post<VideoDTO>('/v1/videos', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },
}
