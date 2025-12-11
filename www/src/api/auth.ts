import apiClient from './index'

export interface AuthStatus {
  status: boolean
}

export const authApi = {
  /**
   * Get VK authorization URL
   */
  getVkAuthUrl(deviceId?: string) {
    return apiClient.get<{ url: string }>('/auth/vkUrl', {
      params: deviceId ? { device_id: deviceId } : undefined,
    })
  },

  /**
   * Get user authentication status
   */
  getAuthStatus() {
    return apiClient.get<AuthStatus>('/auth/status')
  },
}
