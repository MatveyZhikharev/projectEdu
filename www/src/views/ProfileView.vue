<script lang="ts">
import { useUserStore } from '@/stores/user'
import { blocksApi, type BlockResponse } from '@/api/blocks'

export default {
  data() {
    return {
      blocksLoading: false,
      blocks: [] as BlockResponse[],
    }
  },
  computed: {
    userStore() {
      return useUserStore()
    },
    isAuthenticated(): boolean {
      return this.userStore.isAuthenticated
    },
    loading(): boolean {
      return this.userStore.loading
    },
    displayName(): string {
      return this.userStore.displayName
    },
    userEmail(): string | undefined {
      return this.userStore.userEmail
    },
    blocksCount(): number {
      return this.blocks.length
    }
  },
  async mounted() {
    // Initialize store from localStorage
    this.userStore.initFromStorage()
    
    // Check if we have a VK callback
    await this.handleVkCallback()
    
    // Check auth status from API
    await this.userStore.checkAuthStatus()
    
    if (this.isAuthenticated) {
      await this.fetchBlocks()
    }
  },
  methods: {
    async fetchBlocks() {
      this.blocksLoading = true
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        this.blocks = response.data
      } catch (error) {
        console.error('Ошибка загрузки блоков:', error)
      } finally {
        this.blocksLoading = false
      }
    },
    async handleVkLogin() {
      try {
        await this.userStore.loginWithVk()
      } catch (error) {
        console.error('Ошибка получения ссылки VK:', error)
      }
    },
    async handleVkCallback() {
      const query = this.$route.query as {
        code?: string
        state?: string
        device_id?: string
      }

      const { code, state, device_id } = query

      if (!code) {
        return
      }

      try {
        await this.userStore.handleVkCallback(code, state, device_id)
        
        // Clear URL parameters after successful callback
        this.$router.replace({ path: '/profile', query: {} })
      } catch (err: any) {
        console.error('VK callback error:', err)
      }
    }
  }
}
</script>

<template>
  <div class="profile-container">
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Загрузка...</p>
    </div>

    <div v-else-if="!isAuthenticated" class="auth-section">
      <div class="auth-content">
        <div class="auth-icon">👤</div>
        <h1 class="auth-title">Профиль</h1>
        <p class="auth-message">Войдите, чтобы получить доступ к профилю</p>
        <button class="vk-login-btn" @click="handleVkLogin">
          <span class="vk-icon">VK</span>
          Войти через ВКонтакте
        </button>
      </div>
    </div>

    <div v-else class="profile-view">
      <div class="profile-header">
        <div class="profile-avatar">
          <span class="avatar-icon">👤</span>
        </div>
        <div class="profile-info">
          <h1 class="profile-name">{{ displayName }}</h1>
          <p v-if="userEmail" class="profile-email">{{ userEmail }}</p>
          <span class="profile-badge">Студент</span>
        </div>
      </div>

      <div class="profile-stats">
        <div class="stat-card">
          <span class="stat-number">{{ blocksCount }}</span>
          <span class="stat-label">Доступных курсов</span>
        </div>
        <div class="stat-card">
          <span class="stat-number">0</span>
          <span class="stat-label">Завершено</span>
        </div>
        <div class="stat-card">
          <span class="stat-number">0</span>
          <span class="stat-label">Сертификатов</span>
        </div>
      </div>

      <div class="profile-section">
        <h2 class="section-title">Мои курсы</h2>

        <div v-if="blocksLoading" class="section-loading">
          Загрузка...
        </div>

        <div v-else-if="blocks.length === 0" class="empty-courses">
          <p>У вас пока нет курсов</p>
          <router-link to="/catalog" class="browse-link">Перейти в каталог</router-link>
        </div>

        <div v-else class="courses-preview">
          <div
              v-for="block in blocks.slice(0, 3)"
              :key="block.id"
              class="course-preview-item"
          >
            <div class="preview-info">
              <h4>{{ block.title }}</h4>
              <span class="preview-status" :class="{ available: block.isAvailable }">
                {{ block.isAvailable ? 'Доступен' : 'Скоро' }}
              </span>
            </div>
            <div class="preview-progress">
              <div class="mini-progress-bar">
                <div class="mini-progress-fill" style="width: 0%"></div>
              </div>
              <span class="mini-progress-text">0%</span>
            </div>
          </div>

          <router-link v-if="blocks.length > 3" to="/my-courses" class="see-all-link">
            Смотреть все курсы →
          </router-link>
        </div>
      </div>

      <div class="profile-section">
        <h2 class="section-title">Настройки аккаунта</h2>
        <div class="settings-list">
          <div class="settings-item">
            <span class="settings-icon">🔔</span>
            <span class="settings-label">Уведомления</span>
            <span class="settings-status">Включены</span>
          </div>
          <div class="settings-item">
            <span class="settings-icon">🌙</span>
            <span class="settings-label">Темная тема</span>
            <span class="settings-status">Выключена</span>
          </div>
          <div class="settings-item">
            <span class="settings-icon">🔐</span>
            <span class="settings-label">Безопасность</span>
            <span class="settings-status">VK авторизация</span>
          </div>
          <div class="settings-item">
            <span class="settings-icon">👩‍💻</span>
            <span class="settings-label">Админ панель</span>
            <a href="/admin" class="settings-status">перейти</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  width: 100%;
  min-height: 60vh;
  padding: 40px 100px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #666;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f0f0f0;
  border-top-color: #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.auth-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.auth-content {
  text-align: center;
  max-width: 400px;
}

.auth-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.auth-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.auth-message {
  font-size: 16px;
  color: #666;
  margin: 0 0 24px 0;
}

.vk-login-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: #0077ff;
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.vk-login-btn:hover {
  background: #0066dd;
}

.vk-icon {
  font-weight: bold;
  background: white;
  color: #0077ff;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 14px;
}

.profile-view {
  max-width: 900px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
  margin-bottom: 30px;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 48px;
}

.profile-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.profile-email {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 12px 0;
}

.profile-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-number {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: #007bff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.profile-section {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #1a1a1a;
}

.section-loading {
  text-align: center;
  color: #666;
  padding: 20px;
}

.empty-courses {
  text-align: center;
  padding: 30px;
  color: #666;
}

.browse-link {
  display: inline-block;
  margin-top: 12px;
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
}

.browse-link:hover {
  text-decoration: underline;
}

.courses-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.course-preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.preview-info h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-status {
  font-size: 12px;
  color: #999;
}

.preview-status.available {
  color: #2e7d32;
}

.preview-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mini-progress-bar {
  width: 100px;
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: #007bff;
  border-radius: 3px;
}

.mini-progress-text {
  font-size: 13px;
  color: #666;
  min-width: 30px;
}

.see-all-link {
  text-align: center;
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
  padding: 12px;
}

.see-all-link:hover {
  text-decoration: underline;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.settings-icon {
  font-size: 24px;
}

.settings-label {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
}

.settings-status {
  font-size: 14px;
  color: #666;
}
</style>
