<script lang="ts">
import { blocksApi, type BlockResponse } from '@/api/blocks'
import { authApi } from '@/api/auth'

export default {
  data() {
    return {
      loading: true,
      error: null as string | null,
      block: null as BlockResponse | null,
      isAuthenticated: false,
      authChecked: false
    }
  },
  computed: {
    blockId(): number {
      return Number(this.$route.params.id)
    },
    blockImageUrl(): string {
      return this.block ? blocksApi.getBlockImageUrl(this.block.id) : ''
    },
    blockVideoUrl(): string {
      return this.block ? blocksApi.getBlockVideoUrl(this.block.id) : ''
    }
  },
  async mounted() {
    await this.checkAuth()
    await this.fetchBlock()
  },
  methods: {
    async checkAuth() {
      try {
        const response = await authApi.getAuthStatus()
        this.isAuthenticated = response.data.status
      } catch (error) {
        this.isAuthenticated = false
      } finally {
        this.authChecked = true
      }
    },
    async fetchBlock() {
      this.loading = true
      this.error = null
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        const foundBlock = response.data.find(b => b.id === this.blockId)
        if (foundBlock) {
          this.block = foundBlock
        } else {
          this.error = 'Материал не найден'
        }
      } catch (error: any) {
        console.error('Ошибка загрузки материала:', error)
        this.error = 'Не удалось загрузить материал'
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
    goBack() {
      this.$router.back()
    },
    handleStartTest() {
      // Test functionality will be implemented when test API is available
      alert('Функционал тестирования будет доступен в ближайшее время')
    },
    handleMarkComplete() {
      // Mark as complete functionality will be implemented when progress API is available
      alert('Прогресс сохранён! (Функционал будет полностью доступен позже)')
    }
  }
}
</script>

<template>
  <div class="material-container">
    <!-- Back button -->
    <button class="back-btn" @click="goBack">
      ← Назад к курсу
    </button>

    <!-- Loading state -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Загрузка материала...</p>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <h2>{{ error }}</h2>
      <p>Попробуйте вернуться назад и выбрать другой материал</p>
      <button @click="goBack" class="retry-btn">Вернуться назад</button>
    </div>

    <!-- Material content -->
    <div v-else-if="block" class="material-content">
      <!-- Material header -->
      <div class="material-header">
        <div class="material-order">Урок {{ block.sortOrder }}</div>
        <h1 class="material-title">{{ block.title }}</h1>
        <div class="material-status" :class="{ available: block.isAvailable }">
          {{ block.isAvailable ? 'Доступен' : 'Недоступен' }}
        </div>
      </div>

      <!-- Auth check for content -->
      <div v-if="!isAuthenticated && authChecked" class="auth-required">
        <div class="auth-content">
          <div class="auth-icon">🔒</div>
          <h2>Требуется авторизация</h2>
          <p>Войдите, чтобы получить доступ к материалам курса</p>
          <button class="vk-login-btn" @click="handleVkLogin">
            <span class="vk-icon">VK</span>
            Войти через ВКонтакте
          </button>
        </div>
      </div>

      <!-- Content for authenticated users -->
      <div v-else class="content-sections">
        <!-- Video section -->
        <section class="content-section video-section">
          <h2 class="section-title">Видео урок</h2>
          <div class="video-container">
            <video
              controls
              :poster="blockImageUrl"
              class="video-player"
            >
              <source :src="blockVideoUrl" type="video/mp4">
              Ваш браузер не поддерживает воспроизведение видео.
            </video>
          </div>
        </section>

        <!-- Image preview section -->
        <section class="content-section image-section">
          <h2 class="section-title">Превью материала</h2>
          <div class="image-container">
            <img :src="blockImageUrl" :alt="block.title" class="material-image">
          </div>
        </section>

        <!-- Description section -->
        <section class="content-section description-section">
          <h2 class="section-title">Описание</h2>
          <div class="description-content">
            <p>
              Этот урок научит вас основам работы с данной темой. 
              Просмотрите видео выше и выполните практические задания для закрепления материала.
            </p>
            <ul class="learning-goals">
              <li>Изучите теоретическую часть урока</li>
              <li>Просмотрите видеоматериал до конца</li>
              <li>Выполните практические задания</li>
              <li>Пройдите тест для проверки знаний</li>
            </ul>
          </div>
        </section>

        <!-- Test section (if available) -->
        <section v-if="block.testId" class="content-section test-section">
          <h2 class="section-title">Тестирование</h2>
          <div class="test-card">
            <div class="test-icon">📝</div>
            <div class="test-info">
              <h3>Тест по материалу</h3>
              <p>Проверьте свои знания после изучения урока</p>
            </div>
            <button class="start-test-btn" @click="handleStartTest">Начать тест</button>
          </div>
        </section>

        <!-- Navigation buttons -->
        <div class="navigation-buttons">
          <button class="nav-btn prev-btn" @click="goBack">
            ← Назад к курсу
          </button>
          <button class="nav-btn complete-btn" @click="handleMarkComplete">
            Отметить как пройденный ✓
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.material-container {
  padding: 30px 100px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  background: none;
  border: none;
  color: #007bff;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn:hover {
  text-decoration: underline;
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

/* Error state */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.error-state h2 {
  font-size: 24px;
  color: #c62828;
  margin: 0 0 12px 0;
}

.error-state p {
  color: #666;
  margin: 0 0 24px 0;
}

.retry-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.retry-btn:hover {
  background: #0056b3;
}

/* Material header */
.material-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.material-order {
  color: #007bff;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.material-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: #1a1a1a;
}

.material-status {
  display: inline-block;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 16px;
  background: #f5f5f5;
  color: #999;
}

.material-status.available {
  background: #e8f5e9;
  color: #2e7d32;
}

/* Auth required */
.auth-required {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.auth-content {
  text-align: center;
  max-width: 400px;
}

.auth-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.auth-content h2 {
  font-size: 24px;
  margin: 0 0 12px 0;
  color: #333;
}

.auth-content p {
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

/* Content sections */
.content-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.content-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #1a1a1a;
}

/* Video section */
.video-container {
  border-radius: 12px;
  overflow: hidden;
  background: #000;
}

.video-player {
  width: 100%;
  max-height: 500px;
  display: block;
}

/* Image section */
.image-container {
  border-radius: 12px;
  overflow: hidden;
}

.material-image {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  display: block;
}

/* Description section */
.description-content p {
  color: #444;
  line-height: 1.7;
  margin: 0 0 16px 0;
}

.learning-goals {
  list-style: none;
  padding: 0;
  margin: 0;
}

.learning-goals li {
  padding: 12px 0 12px 32px;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
  color: #444;
}

.learning-goals li:last-child {
  border-bottom: none;
}

.learning-goals li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #2e7d32;
  font-weight: bold;
}

/* Test section */
.test-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.test-icon {
  font-size: 40px;
}

.test-info {
  flex: 1;
}

.test-info h3 {
  margin: 0 0 6px 0;
  font-size: 18px;
  color: #1a1a1a;
}

.test-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.start-test-btn {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.start-test-btn:hover {
  background: #138496;
}

/* Navigation buttons */
.navigation-buttons {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 0;
}

.nav-btn {
  padding: 14px 28px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.prev-btn {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #e0e0e0;
}

.prev-btn:hover {
  background: #e0e0e0;
}

.complete-btn {
  background: #28a745;
  color: white;
  border: none;
}

.complete-btn:hover {
  background: #218838;
}
</style>
