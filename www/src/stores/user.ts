import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

export interface User {
  firstName?: string
  lastName?: string
  email?: string
  vkId?: number
}

interface UserState {
  user: User | null
  isAuthenticated: boolean
  loading: boolean
}

const USER_STORAGE_KEY = 'user_data'
const AUTH_STORAGE_KEY = 'auth_status'

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    isAuthenticated: false,
    loading: false,
  }),

  getters: {
    displayName(): string {
      if (this.user?.firstName || this.user?.lastName) {
        return `${this.user.firstName || ''} ${this.user.lastName || ''}`.trim()
      }
      return 'Пользователь'
    },
    userEmail(): string | undefined {
      return this.user?.email
    },
  },

  actions: {
    // Initialize store from localStorage
    initFromStorage() {
      try {
        const storedUser = localStorage.getItem(USER_STORAGE_KEY)
        const storedAuth = localStorage.getItem(AUTH_STORAGE_KEY)

        if (storedUser) {
          const parsed = JSON.parse(storedUser)
          // Validate parsed user object has expected structure
          if (parsed && typeof parsed === 'object') {
            this.user = {
              firstName: typeof parsed.firstName === 'string' ? parsed.firstName : undefined,
              lastName: typeof parsed.lastName === 'string' ? parsed.lastName : undefined,
              email: typeof parsed.email === 'string' ? parsed.email : undefined,
              vkId: typeof parsed.vkId === 'number' ? parsed.vkId : undefined,
            }
          }
        }
        if (storedAuth) {
          const parsedAuth = JSON.parse(storedAuth)
          this.isAuthenticated = parsedAuth === true
        }
      } catch (error) {
        console.error('Error loading user from localStorage:', error)
        this.clearStorage()
      }
    },

    // Save current state to localStorage
    saveToStorage() {
      try {
        // Save both values atomically - clear first if user is null
        if (this.user) {
          localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(this.user))
        } else {
          localStorage.removeItem(USER_STORAGE_KEY)
        }
        localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(this.isAuthenticated))
      } catch (error) {
        console.error('Error saving user to localStorage:', error)
        // On failure, try to clear to maintain consistency
        this.clearStorage()
      }
    },

    // Clear localStorage
    clearStorage() {
      localStorage.removeItem(USER_STORAGE_KEY)
      localStorage.removeItem(AUTH_STORAGE_KEY)
    },

    // Check authentication status from API
    async checkAuthStatus() {
      this.loading = true
      try {
        const response = await authApi.getAuthStatus()
        const data = response.data as { status: boolean }
        this.isAuthenticated = data.status

        if (this.isAuthenticated) {
          await this.fetchUser()
        } else {
          this.user = null
          this.clearStorage()
        }

        this.saveToStorage()
        return this.isAuthenticated
      } catch (error) {
        console.error('Error checking auth status:', error)
        this.isAuthenticated = false
        this.user = null
        this.clearStorage()
        return false
      } finally {
        this.loading = false
      }
    },

    // Fetch user data from API
    async fetchUser() {
      try {
        const response = await authApi.getUser()
        const data = response.data as User
        if (data) {
          this.user = data
          this.saveToStorage()
        }
      } catch (error) {
        console.error('Error fetching user:', error)
      }
    },

    // Get VK auth URL and redirect
    async loginWithVk() {
      try {
        const response = await authApi.getVkAuthUrl()
        window.location.href = response.data.url
      } catch (error) {
        console.error('Error getting VK auth URL:', error)
        throw error
      }
    },

    // Handle VK callback
    async handleVkCallback(code: string, state?: string, deviceId?: string) {
      try {
        await authApi.getVkCallback(code, state, deviceId)
        await this.checkAuthStatus()
        return true
      } catch (error) {
        console.error('VK callback error:', error)
        throw error
      }
    },

    // Logout user
    logout() {
      this.user = null
      this.isAuthenticated = false
      this.clearStorage()
    },

    // Set user data manually (for testing or direct updates)
    setUser(user: User) {
      this.user = user
      this.isAuthenticated = true
      this.saveToStorage()
    },
  },
})
