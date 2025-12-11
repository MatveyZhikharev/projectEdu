<script lang="ts">
import { blocksApi, type BlockResponse } from '@/api/blocks'

export default {
  data() {
    return {
      loading: false,
      error: null as string | null,
      blocks: [] as BlockResponse[]
    }
  },
  computed: {
    blocksCount(): number {
      return this.blocks.length
    }
  },
  async mounted() {
    await this.fetchBlocks()
  },
  methods: {
    async fetchBlocks() {
      this.loading = true
      this.error = null
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        this.blocks = response.data
      } catch (error: any) {
        console.error('Ошибка загрузки курсов:', error)
        this.error = 'Не удалось загрузить каталог курсов'
      } finally {
        this.loading = false
      }
    },
    getBlockImageUrl(blockId: number) {
      return blocksApi.getBlockImageUrl(blockId)
    }
  }
}
</script>

<template>
  <div class="catalog-container">
    <div class="catalog-header">
      <h1 class="catalog-title">Каталог курсов</h1>
      <p class="catalog-description">
        Выберите интересующий вас курс и начните обучение прямо сейчас
      </p>
      <div class="catalog-stats" v-if="!loading && !error">
        <span class="stats-item">{{ blocksCount }} доступных курсов</span>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Загрузка каталога...</p>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <p>{{ error }}</p>
      <button @click="fetchBlocks" class="retry-btn">Попробовать снова</button>
    </div>

    <!-- Empty state -->
    <div v-else-if="blocks.length === 0" class="empty-state">
      <div class="empty-icon">📚</div>
      <h2>Курсы пока не добавлены</h2>
      <p>Скоро здесь появятся интересные курсы!</p>
    </div>

    <!-- Courses grid -->
    <div v-else class="courses-grid">
      <router-link 
        v-for="block in blocks" 
        :key="block.id"
        :to="{ name: 'material', params: { id: String(block.id) } }"
        class="course-card"
      >
        <div 
          class="course-image" 
          :style="{ backgroundImage: `url(${getBlockImageUrl(block.id)})` }"
        >
          <span class="course-order">{{ block.sortOrder }}</span>
        </div>
        <div class="course-info">
          <h3 class="course-title">{{ block.title }}</h3>
          <div class="course-meta">
            <span 
              class="course-status" 
              :class="{ available: block.isAvailable }"
            >
              {{ block.isAvailable ? 'Доступен' : 'Скоро' }}
            </span>
          </div>
          <span 
            class="enroll-btn" 
            :class="{ disabled: !block.isAvailable }"
          >
            {{ block.isAvailable ? 'Начать обучение' : 'Скоро будет доступен' }}
          </span>
        </div>
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.catalog-container {
  padding: 40px 100px;
  width: 100%;
}

.catalog-header {
  text-align: center;
  margin-bottom: 40px;
}

.catalog-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #1a1a1a;
}

.catalog-description {
  font-size: 18px;
  color: #666;
  margin: 0 0 20px 0;
}

.catalog-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.stats-item {
  background: #f0f7ff;
  color: #007bff;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

/* Loading state */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
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
  min-height: 300px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-state p {
  color: #c62828;
  font-size: 16px;
  margin: 0 0 20px 0;
}

.retry-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.retry-btn:hover {
  background: #0056b3;
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
  margin: 0;
}

/* Courses grid */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
}

.course-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  text-decoration: none;
  color: inherit;
  display: block;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.course-image {
  position: relative;
  height: 200px;
  background-color: #f5f5f5;
  background-size: cover;
  background-position: center;
}

.course-order {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
}

.course-info {
  padding: 20px;
}

.course-info .course-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #1a1a1a;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.course-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  background: #f5f5f5;
  color: #999;
}

.course-status.available {
  background: #e8f5e9;
  color: #2e7d32;
}

.enroll-btn {
  width: 100%;
  background: #007bff;
  color: white;
  border: none;
  padding: 14px 20px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  display: block;
  text-align: center;
}

.enroll-btn:hover:not(.disabled) {
  background: #0056b3;
}

.enroll-btn.disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
}
</style>
