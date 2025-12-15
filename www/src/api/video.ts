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
  getAllVideos() {
    return apiClient.get<VideoDTO[]>('/v1/videos')
  },

  getVideoInfo(id: number) {
    return apiClient.get<VideoInfoDTO>(`/v1/videos/${id}`)
  },

  getVideoChunk(id: number, chunkIndex: number) {
    return apiClient.get<ChunkResponseDTO>(`/v1/videos/${id}/stream/${chunkIndex}`)
  },

  getVideoContentType(id: number) {
    return apiClient.get<string>(`/v1/videos/${id}/content-type`)
  },

  getVideoStreamUrl(id: number) {
    return `/api/v1/videos/${id}/stream`
  },

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
