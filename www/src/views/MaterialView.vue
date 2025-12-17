<script lang="ts">
import { blocksApi, type BlockResponse, type VideoInfoResponse } from '@/api/blocks'
import { testsApi, type TestData, type Question, type Answer } from '@/api/tests'
import { useUserStore } from '@/stores/user'
import { revokeVideoBlobUrl } from '@/utils/videoDecryption'

export default {
  data() {
    return {
      loading: true,
      error: null as string | null,
      block: null as BlockResponse | null,
      videoInfo: null as VideoInfoResponse | null,
      videoLoading: false,
      videoError: null as string | null,
      // Video URL for streaming
      videoUrl: null as string | null,
      // Chunked streaming state
      isChunkedStreaming: false,
      chunkLoadingProgress: 0,
      useAdminStream: false, // Use admin direct stream (for admins only)
      // Test state
      testData: null as TestData | null,
      testLoading: false,
      testStarted: false,
      selectedAnswers: {} as Record<number, number>, // questionId -> answerId
      testSubmitted: false,
      testResult: null as { score: number; total: number; passed: boolean } | null,
    }
  },
  computed: {
    userStore() {
      return useUserStore()
    },
    isAuthenticated(): boolean {
      return this.userStore.isAuthenticated
    },
    authChecked(): boolean {
      return !this.userStore.loading
    },
    blockId(): number {
      return Number(this.$route.params.id)
    },
    blockImageUrl(): string {
      return this.block ? blocksApi.getBlockImageUrl(this.block.id) : ''
    },
    hasVideo(): boolean {
      return this.videoInfo !== null && this.videoInfo.status === 'READY'
    },
    videoDescription(): string {
      return this.videoInfo?.description || 'Описание материала будет добавлено позже.'
    },
    videoDuration(): string {
      return this.videoInfo?.formattedDuration || '00:00'
    },
    videoSize(): string {
      return this.videoInfo?.formattedFileSize || ''
    },
    isAdmin(): boolean {
      // Check if user has admin role (stored in user store)
      return this.userStore.user?.role === 'ADMIN'
    },
    // Test block check
    isTestBlock(): boolean {
      return this.block?.testId != null
    },
    // Pure test block (test without video)
    isPureTestBlock(): boolean {
      return this.isTestBlock && !this.hasVideo
    },
    // Check if all questions are answered
    allQuestionsAnswered(): boolean {
      if (!this.testData) return false
      return this.testData.questions.every(q => this.selectedAnswers[q.id] !== undefined)
    }
  },
  async mounted() {
    // Initialize store from localStorage
    this.userStore.initFromStorage()
    
    // Check auth status from API
    await this.userStore.checkAuthStatus()
    
    await this.fetchBlock()
  },
  beforeUnmount() {
    // Clean up video resources
    this.cleanupVideo()
  },
  methods: {
    async fetchBlock() {
      this.loading = true
      this.error = null
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        const foundBlock = response.data.find(b => b.id === this.blockId)
        if (foundBlock) {
          this.block = foundBlock
          // Fetch video info after block is loaded
          await this.fetchVideoInfo()
        } else {
          this.error = 'Материал не найден'
        }
      } catch (error: unknown) {
        console.error('Ошибка загрузки материала:', error)
        this.error = 'Не удалось загрузить материал'
      } finally {
        this.loading = false
      }
    },

    async fetchVideoInfo() {
      if (!this.block) return
      
      this.videoLoading = true
      this.videoError = null
      try {
        const response = await blocksApi.getBlockVideoInfo(this.block.id)
        this.videoInfo = response.data
        
        if (this.videoInfo && this.videoInfo.status === 'READY') {
          // Try admin direct stream first (if user is admin)
          // For regular users, use chunked streaming with decryption
          if (this.isAdmin) {
            // Admins can use direct stream
            this.useAdminStream = true
            this.videoUrl = blocksApi.getBlockVideoStreamUrl(this.block.id)
          } else {
            // Regular users use chunked streaming
            await this.initChunkedStreaming()
          }
        }
      } catch (error: unknown) {
        // Video not found is expected for blocks without video
        console.error('Видео для этого блока не найдено или ещё не загружено')
        this.videoInfo = null
      } finally {
        this.videoLoading = false
      }
    },

    async initChunkedStreaming() {
      if (!this.block || !this.videoInfo) return
      
      this.isChunkedStreaming = true
      this.chunkLoadingProgress = 0
      
      try {
        const totalChunks = this.videoInfo.totalChunks
        const mimeType = this.videoInfo.mimeType || 'video/mp4'
        
        // Try to load chunks sequentially
        const chunks: Uint8Array[] = []
        
        for (let i = 0; i < totalChunks; i++) {
          try {
            const chunkResponse = await blocksApi.getBlockVideoChunk(this.block.id, i)
            const chunk = chunkResponse.data
            
            // Check if we have IV for decryption
            if (chunk.iv && chunk.encryptedData) {
              // NOTE: Decryption key must be provided by backend
              // Currently the backend doesn't expose the decryption key
              // We need an endpoint like GET /api/video/{blockId}/key
              console.log(`Chunk ${i} received with IV, but decryption key is not available from backend`)
              
              // Fallback to admin stream since we can't decrypt
              this.videoError = 'Расшифровка видео требует обновления на сервере. Используется прямой стриминг.'
              this.isChunkedStreaming = false
              this.videoUrl = blocksApi.getBlockVideoStreamUrl(this.block.id)
              return
            } else if (chunk.encryptedData) {
              // Encrypted data without IV - backend bug, use fallback
              console.log(`Chunk ${i} has encrypted data but no IV`)
              this.videoError = 'Ошибка формата видео данных. Используется прямой стриминг.'
              this.isChunkedStreaming = false
              this.videoUrl = blocksApi.getBlockVideoStreamUrl(this.block.id)
              return
            }
            // If we somehow get unencrypted data, we would add it here
            // But based on backend code, all chunks should be encrypted
            
            this.chunkLoadingProgress = Math.round(((i + 1) / totalChunks) * 100)
          } catch (chunkError) {
            console.error(`Error loading chunk ${i}:`, chunkError)
            // If chunk loading fails, fallback to admin stream
            this.videoError = 'Ошибка загрузки видео чанков. Используется прямой стриминг.'
            this.isChunkedStreaming = false
            this.videoUrl = blocksApi.getBlockVideoStreamUrl(this.block.id)
            return
          }
        }
        
        // Create video blob from successfully loaded chunks
        if (chunks.length > 0) {
          const blobParts = chunks.map(chunk => chunk.buffer as ArrayBuffer)
          const blob = new Blob(blobParts, { type: mimeType })
          this.videoUrl = URL.createObjectURL(blob)
        } else {
          // No chunks loaded - use admin stream fallback
          this.videoError = 'Не удалось загрузить видео чанки. Используется прямой стриминг.'
          this.isChunkedStreaming = false
          this.videoUrl = blocksApi.getBlockVideoStreamUrl(this.block.id)
        }
        
      } catch (error) {
        console.error('Error initializing chunked streaming:', error)
        this.videoError = 'Не удалось загрузить видео'
        this.isChunkedStreaming = false
        
        // Fallback to admin stream
        this.videoUrl = blocksApi.getBlockVideoStreamUrl(this.block.id)
      }
    },

    cleanupVideo() {
      if (this.videoUrl && this.videoUrl.startsWith('blob:')) {
        revokeVideoBlobUrl(this.videoUrl)
      }
      this.videoUrl = null
      this.isChunkedStreaming = false
      this.chunkLoadingProgress = 0
    },

    async handleVkLogin() {
      try {
        await this.userStore.loginWithVk()
      } catch (error) {
        console.error('Ошибка получения ссылки VK:', error)
      }
    },
    goBack() {
      this.$router.back()
    },
    
    // Test methods
    async handleStartTest() {
      if (!this.block?.testId) return
      
      this.testLoading = true
      
      try {
        // Try to fetch test data from API
        const response = await testsApi.getTest(this.block.testId)
        this.testData = response.data
        this.testStarted = true
      } catch {
        // API not available - use mock data for development
        console.log('Test API not available, using mock data')
        this.testData = testsApi.getMockTestData(this.block.testId)
        this.testStarted = true
      } finally {
        this.testLoading = false
      }
    },
    
    selectAnswer(questionId: number, answerId: number) {
      if (this.testSubmitted) return
      this.selectedAnswers = { ...this.selectedAnswers, [questionId]: answerId }
    },
    
    submitTest() {
      if (!this.testData || !this.allQuestionsAnswered) return
      
      let correctCount = 0
      const total = this.testData.questions.length
      
      this.testData.questions.forEach(question => {
        const selectedAnswerId = this.selectedAnswers[question.id]
        const correctAnswer = question.answers.find(a => a.isRight)
        if (correctAnswer && selectedAnswerId === correctAnswer.id) {
          correctCount++
        }
      })
      
      const score = Math.round((correctCount / total) * 100)
      const passed = score >= this.testData.passPercent
      
      this.testResult = { score, total: correctCount, passed }
      this.testSubmitted = true
    },
    
    resetTest() {
      this.testStarted = false
      this.testSubmitted = false
      this.selectedAnswers = {}
      this.testResult = null
      this.testData = null
    },
    
    handleMarkComplete() {
      alert('Прогресс сохранён! (Функционал будет полностью доступен позже)')
    }
  }
}
</script>

<template>
  <div class="material-container">
    <button class="back-btn" @click="goBack">
      ← Назад к курсу
    </button>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Загрузка материала...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <h2>{{ error }}</h2>
      <p>Попробуйте вернуться назад и выбрать другой материал</p>
      <button @click="goBack" class="retry-btn">Вернуться назад</button>
    </div>

    <div v-else-if="block" class="material-content">
      <div class="material-header">
        <span class="material-order">Урок {{ block.sortOrder }}</span>
        <h1 class="material-title">{{ block.title }}</h1>
        <span class="material-status" :class="{ available: block.isAvailable }">
          {{ block.isAvailable ? 'Доступен' : 'Недоступен' }}
        </span>
      </div>

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

      <div v-else class="content-sections">
        <section class="video-section video-section video-section">
          <h2 class="section-title">
            Видео урок
            <span v-if="videoInfo" class="video-meta">
              ({{ videoDuration }} · {{ videoSize }})
            </span>
            <span v-if="isChunkedStreaming" class="streaming-badge">Чанковый стриминг</span>
          </h2>
          
          <!-- Chunk loading progress -->
          <div v-if="isChunkedStreaming && chunkLoadingProgress < 100" class="chunk-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: chunkLoadingProgress + '%' }"></div>
            </div>
            <p class="progress-text">Загрузка видео: {{ chunkLoadingProgress }}%</p>
          </div>
          
          <!-- Video player with streaming -->
          <div v-if="videoUrl" class="video-container">
            <video
              controls
              :poster="blockImageUrl"
              class="video-player"
              :src="videoUrl"
              height="540px"
              width="960px"
            >
              Ваш браузер не поддерживает воспроизведение видео.
            </video>
            <p v-if="videoError" class="video-warning">{{ videoError }}</p>
          </div>
          
          <!-- Fallback when no video available -->
          <div v-else-if="!hasVideo && !videoLoading && !isChunkedStreaming" class="video-placeholder">
            <div class="placeholder-content">
              <div class="placeholder-icon">🎬</div>
              <p>Видео для этого урока ещё не загружено</p>
              <img :src="blockImageUrl" :alt="block.title" class="placeholder-image">
            </div>
          </div>
          
          <!-- Video loading state -->
          <div v-else class="video-loading">
            <div class="loading-spinner small"></div>
            <p>Загрузка информации о видео...</p>
          </div>
        </section>

        <section class="content-section description-section">
          <h2 class="section-title">Описание</h2>
          <div class="description-content">
            <p class="video-description">{{ videoDescription }}</p>
            
            <div v-if="videoInfo" class="video-details">
              <h3>Информация о видео</h3>
              <ul class="details-list">
                <li><strong>Длительность:</strong> {{ videoDuration }}</li>
                <li><strong>Размер:</strong> {{ videoSize }}</li>
                <li><strong>Формат:</strong> {{ videoInfo.format }}</li>
                <li><strong>Статус:</strong> {{ videoInfo.status === 'READY' ? 'Готово к просмотру' : 'Обрабатывается' }}</li>
              </ul>
            </div>
            
            <h3>Цели обучения</h3>
            <ul class="learning-goals">
              <li>Изучите теоретическую часть урока</li>
              <li>Просмотрите видеоматериал до конца</li>
              <li>Выполните практические задания</li>
              <li v-if="block.testId">Пройдите тест для проверки знаний</li>
            </ul>
          </div>
        </section>

        <section v-if="block.testId" class="content-section test-section">
          <h2 class="section-title">
            Тестирование
            <span v-if="isPureTestBlock" class="test-type-badge">Блок-тест</span>
          </h2>
          
          <!-- Test not started -->
          <div v-if="!testStarted && !testLoading" class="test-card">
            <div class="test-icon">📝</div>
            <div class="test-info">
              <h3>Тест по материалу</h3>
              <p>Проверьте свои знания после изучения урока</p>
              <p class="test-hint">Проходной балл будет указан после начала теста</p>
            </div>
            <button class="start-test-btn" @click="handleStartTest">Начать тест</button>
          </div>
          
          <!-- Test loading -->
          <div v-if="testLoading" class="test-loading">
            <div class="loading-spinner small"></div>
            <p>Загрузка вопросов...</p>
          </div>
          
          <!-- Test in progress -->
          <div v-if="testStarted && testData && !testSubmitted" class="test-content">
            <div class="test-progress">
              <span>Вопросов: {{ Object.keys(selectedAnswers).length }} / {{ testData.questions.length }}</span>
              <span class="pass-requirement">Проходной балл: {{ testData.passPercent }}%</span>
            </div>
            
            <div class="questions-list">
              <div v-for="(question, index) in testData.questions" :key="question.id" class="question-card">
                <div class="question-header">
                  <span class="question-number">Вопрос {{ index + 1 }}</span>
                  <span v-if="selectedAnswers[question.id]" class="question-answered">✓ Отвечено</span>
                </div>
                <p class="question-text">{{ question.questionText }}</p>
                
                <div class="answers-list">
                  <label 
                    v-for="answer in question.answers" 
                    :key="answer.id"
                    class="answer-option"
                    :class="{ selected: selectedAnswers[question.id] === answer.id }"
                  >
                    <input 
                      type="radio" 
                      :name="'question-' + question.id"
                      :value="answer.id"
                      :checked="selectedAnswers[question.id] === answer.id"
                      @change="selectAnswer(question.id, answer.id)"
                    >
                    <span class="answer-text">{{ answer.answerText }}</span>
                  </label>
                </div>
              </div>
            </div>
            
            <div class="test-actions">
              <button class="cancel-test-btn" @click="resetTest">Отменить</button>
              <button 
                class="submit-test-btn" 
                :disabled="!allQuestionsAnswered"
                @click="submitTest"
              >
                Завершить тест
              </button>
            </div>
          </div>
          
          <!-- Test result -->
          <div v-if="testSubmitted && testResult" class="test-result">
            <div class="result-icon" :class="{ passed: testResult.passed, failed: !testResult.passed }">
              {{ testResult.passed ? '🎉' : '😔' }}
            </div>
            <h3 class="result-title">
              {{ testResult.passed ? 'Тест пройден!' : 'Тест не пройден' }}
            </h3>
            <div class="result-score">
              <div class="score-circle" :class="{ passed: testResult.passed }">
                {{ testResult.score }}%
              </div>
              <p class="score-details">
                Правильных ответов: {{ testResult.total }} из {{ testData?.questions.length }}
              </p>
            </div>
            <div class="result-actions">
              <button class="retry-test-btn" @click="resetTest">
                {{ testResult.passed ? 'Пройти снова' : 'Попробовать ещё раз' }}
              </button>
              <button v-if="testResult.passed" class="continue-btn" @click="goBack">
                Продолжить обучение
              </button>
            </div>
          </div>
        </section>

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

.video-section {
  background: white;
  border-radius: 16px;
  padding-top: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #1a1a1a;
}

.video-container {
  border-radius: 12px;
  overflow: hidden;
  background: #000;
}

.video-player {
  width: 100%;
  display: block;
}

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

/* Video meta info in title */
.video-meta {
  font-size: 14px;
  font-weight: 400;
  color: #666;
  margin-left: 8px;
}

/* Video loading state */
.video-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 40px;
}

.video-loading p {
  color: #666;
  margin-top: 16px;
}

.loading-spinner.small {
  width: 32px;
  height: 32px;
  border-width: 3px;
}

/* Video placeholder when no video available */
.video-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background: #f8f9fa;
  border-radius: 12px;
}

.placeholder-content {
  text-align: center;
  padding: 40px;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.placeholder-content p {
  color: #666;
  margin-bottom: 20px;
}

.placeholder-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  object-fit: cover;
}

/* Video description styling */
.video-description {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 24px;
  white-space: pre-wrap;
}

/* Video details section */
.video-details {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.video-details h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #1a1a1a;
}

.details-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.details-list li {
  font-size: 14px;
  color: #444;
}

.details-list strong {
  color: #1a1a1a;
}

.description-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 24px 0 12px 0;
  color: #1a1a1a;
}

/* Chunk streaming styles */
.streaming-badge {
  display: inline-block;
  background: #17a2b8;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  margin-left: 10px;
  vertical-align: middle;
}

.chunk-progress {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #007bff, #00c6ff);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  margin: 12px 0 0 0;
  font-size: 14px;
  color: #666;
  text-align: center;
}

.video-warning {
  margin-top: 12px;
  padding: 12px 16px;
  background: #fff3cd;
  color: #856404;
  border-radius: 8px;
  font-size: 14px;
}

/* Test styling */
.test-type-badge {
  display: inline-block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-left: 10px;
  vertical-align: middle;
}

.test-hint {
  font-size: 13px;
  color: #888;
  margin-top: 8px;
}

.test-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

.test-loading p {
  margin-top: 16px;
  color: #666;
}

.test-content {
  padding: 20px 0;
}

.test-progress {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #444;
}

.pass-requirement {
  color: #17a2b8;
  font-weight: 500;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 24px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-number {
  font-size: 13px;
  color: #007bff;
  font-weight: 600;
  text-transform: uppercase;
}

.question-answered {
  font-size: 12px;
  color: #28a745;
  font-weight: 500;
}

.question-text {
  font-size: 18px;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  line-height: 1.5;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.answer-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.answer-option:hover {
  background: #f8f9fa;
  border-color: #007bff;
}

.answer-option.selected {
  background: #e7f3ff;
  border-color: #007bff;
}

.answer-option input[type="radio"] {
  width: 20px;
  height: 20px;
  accent-color: #007bff;
}

.answer-text {
  font-size: 15px;
  color: #333;
}

.test-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.cancel-test-btn {
  padding: 14px 28px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-test-btn:hover {
  background: #e0e0e0;
}

.submit-test-btn {
  padding: 14px 32px;
  background: #17a2b8;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-test-btn:hover {
  background: #138496;
}

.submit-test-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* Test result */
.test-result {
  text-align: center;
  padding: 40px 20px;
}

.result-icon {
  font-size: 72px;
  margin-bottom: 20px;
}

.result-icon.passed {
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.result-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 24px 0;
}

.result-icon.passed + .result-title {
  color: #28a745;
}

.result-icon.failed + .result-title {
  color: #dc3545;
}

.result-score {
  margin-bottom: 32px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  margin: 0 auto 16px;
  background: #f0f0f0;
  color: #dc3545;
}

.score-circle.passed {
  background: #d4edda;
  color: #28a745;
}

.score-details {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.retry-test-btn {
  padding: 14px 28px;
  background: #f5f5f5;
  color: #333;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-test-btn:hover {
  background: #e0e0e0;
}

.continue-btn {
  padding: 14px 28px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.continue-btn:hover {
  background: #218838;
}
</style>
