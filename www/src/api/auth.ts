import apiClient from './index'

export interface AuthStatus {
  status: boolean
}

export const authApi = {
  getAuthStatus() {
    return apiClient.get<AuthStatus>('/auth/status')
  },

  getVkAuthUrl(deviceId?: string) {
    return apiClient.get<{ url: string }>('/auth/vkUrl', {
      params: deviceId ? { device_id: deviceId } : undefined,
    })
  },

  getVkCallback(code: string, state: string | undefined, deviceId: string | undefined) {
    return apiClient.get<{ url: string }>('/auth/vkCallback', {
      params: {
        code: code,
        state: state,
        device_id: deviceId
      },
      withCredentials: true,
    })
  },

  getUser() {
    return apiClient.get<AuthStatus>('/auth/user')
  },
}
