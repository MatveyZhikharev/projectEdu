<script lang="ts">
import { blocksApi, type BlockResponse } from '@/api/blocks'
import { authApi } from '@/api/auth'

export default {
  data() {
    return {
      isAuthenticated: false,
      authLoading: true,
      loading: false,
      error: null as string | null,
      blocks: [] as BlockResponse[]
    }
  },
  computed: {
    blocksCount(): number {
      return this.blocks.length
    },
    completedCount(): number {
      // In a real app, this would come from a user progress API
      return 0
    },
    inProgressCount(): number {
      return this.blocksCount
    }
  },
  async mounted() {
    await this.checkAuth()
    if (this.isAuthenticated) {
      await this.fetchBlocks()
    }
  },
  methods: {
    async checkAuth() {
      try {
        const response = await authApi.getAuthStatus()
        this.isAuthenticated = response.data.status
      } catch (error) {
        this.isAuthenticated = false
      } finally {
        this.authLoading = false
      }
    },
    async fetchBlocks() {
      this.loading = true
      this.error = null
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        this.blocks = response.data
      } catch (error: any) {
        console.error('Ошибка загрузки курсов:', error)
        this.error = 'Не удалось загрузить ваши курсы'
      } finally {
        this.loading = false
      }
    },
    async handleVkLogin() {
      try {
        const response = await authApi.getVkAuthUrl()
        window.location.href = response.data.url
      } catch (error) {
        console.error('Ошибка получения ссылки VK:', error)
      }
    },
    getBlockImageUrl(blockId: number) {
      return blocksApi.getBlockImageUrl(blockId)
    }
  }
}
</script>

<template>
  <div class="my-courses-container">
    <!-- Auth loading -->
    <div v-if="authLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Загрузка...</p>
    </div>

    <!-- Not authenticated -->
    <div v-else-if="!isAuthenticated" class="auth-prompt">
      <div class="auth-content">
        <div class="auth-icon">🔒</div>
        <h1>Мои курсы</h1>
        <p class="auth-message">Войдите, чтобы увидеть ваши курсы</p>
        <button class="vk-login-btn" @click="handleVkLogin">
          <span class="vk-icon">VK</span>
          Войти через ВКонтакте
        </button>
      </div>
    </div>

    <!-- Authenticated view -->
    <div v-else class="courses-view">
      <div class="courses-header">
        <h1 class="courses-title">Мои курсы</h1>
        <p class="courses-subtitle">Отслеживайте прогресс и продолжайте обучение</p>
        
        <div class="progress-stats">
          <div class="stat-card">
            <span class="stat-number">{{ blocksCount }}</span>
            <span class="stat-label">Всего курсов</span>
          </div>
          <div class="stat-card">
            <span class="stat-number">{{ inProgressCount }}</span>
            <span class="stat-label">В процессе</span>
          </div>
          <div class="stat-card">
            <span class="stat-number">{{ completedCount }}</span>
            <span class="stat-label">Завершено</span>
          </div>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Загрузка курсов...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-state">
        <p>{{ error }}</p>
        <button @click="fetchBlocks" class="retry-btn">Попробовать снова</button>
      </div>

      <!-- Empty state -->
      <div v-else-if="blocks.length === 0" class="empty-state">
        <div class="empty-icon">📚</div>
        <h2>У вас пока нет курсов</h2>
        <p>Перейдите в каталог, чтобы выбрать интересные курсы!</p>
        <router-link to="/catalog" class="browse-btn">Перейти в каталог</router-link>
      </div>

      <!-- Courses list -->
      <div v-else class="courses-list">
        <div 
          v-for="block in blocks" 
          :key="block.id"
          class="course-item"
        >
          <div 
            class="course-image" 
            :style="{ backgroundImage: `url(${getBlockImageUrl(block.id)})` }"
          ></div>
          <div class="course-details">
            <h3 class="course-title">{{ block.title }}</h3>
            <div class="course-progress">
              <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
              </div>
              <span class="progress-text">0% завершено</span>
            </div>
            <button class="continue-btn">Продолжить обучение</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-courses-container {
  padding: 40px 100px;
  width: 100%;
}

/* Loading state */
.loading-state {
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

/* Auth prompt */
.auth-prompt {
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

.auth-content h1 {
  font-size: 32px;
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

/* Courses view */
.courses-header {
  text-align: center;
  margin-bottom: 40px;
}

.courses-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: #1a1a1a;
}

.courses-subtitle {
  font-size: 18px;
  color: #666;
  margin: 0 0 30px 0;
}

.progress-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.stat-card {
  background: #f8f9fa;
  padding: 20px 40px;
  border-radius: 12px;
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #007bff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* Error state */
.error-state {
  text-align: center;
  padding: 40px;
  color: #c62828;
}

.retry-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 16px;
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 12px 0;
}

.empty-state p {
  font-size: 16px;
  color: #666;
  margin: 0 0 24px 0;
}

.browse-btn {
  background: #007bff;
  color: white;
  text-decoration: none;
  padding: 14px 28px;
  border-radius: 10px;
  font-size: 16px;
  transition: background 0.3s;
}

.browse-btn:hover {
  background: #0056b3;
}

/* Courses list */
.courses-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-item {
  display: flex;
  gap: 24px;
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s;
}

.course-item:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.course-image {
  width: 200px;
  height: 140px;
  background-color: #f5f5f5;
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.course-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.course-details .course-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #1a1a1a;
}

.course-progress {
  margin-bottom: 16px;
}

.progress-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: #007bff;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 13px;
  color: #666;
}

.continue-btn {
  align-self: flex-start;
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.continue-btn:hover {
  background: #0056b3;
}
</style>
