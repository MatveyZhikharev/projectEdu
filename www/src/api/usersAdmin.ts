import apiClient from './index'

export type UserStatus = 'REGISTERED' | 'ACTIVE' | 'BLOCKED'
export type UserRole = 'USER' | 'ADMIN'

export interface User {
  id: string
  vkId: number
  email: string
  firstName: string
  lastName: string
  status: UserStatus
  registrationDate: string
  paymentDate: string | null
  role: UserRole
}

export interface UserUpdateDto {
  firstName?: string
  lastName?: string
  email?: string
  status?: UserStatus
  role?: UserRole
  paymentDate?: string | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export const usersAdminApi = {
  /**
   * Get users with pagination
   */
  getUsers(page: number = 0, size: number = 10) {
    return apiClient.get<PageResponse<User>>('/admin/users', {
      params: { page, size },
    })
  },

  /**
   * Update user
   */
  updateUser(userId: string, data: UserUpdateDto) {
    return apiClient.patch<User>(`/admin/users/${userId}`, data)
  },
}
