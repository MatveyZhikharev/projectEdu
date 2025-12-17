<script lang="ts">
import { blocksApi, type BlockResponse, type VideoInfoResponse } from '@/api/blocks'

interface BlockWithMedia extends BlockResponse {
  hasVideo?: boolean
  videoInfo?: VideoInfoResponse | null
  isTestBlock?: boolean // Block is a test (has testId but no video)
}

export default {
  data() {
    return {
      isMaterialVisible: true,
      loading: false,
      error: null as string | null,
      blocks: [] as BlockWithMedia[]
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
    toggleMaterials() {
      this.isMaterialVisible = !this.isMaterialVisible
    },
    async fetchBlocks() {
      this.loading = true
      this.error = null
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        this.blocks = response.data.map(block => ({ 
          ...block, 
          hasVideo: false, 
          videoInfo: null,
          // Block is a test if it has testId
          isTestBlock: block.testId != null
        }))
        
        // Check video availability for each block
        await this.checkVideoForBlocks()
      } catch (error: unknown) {
        console.error('Ошибка загрузки блоков:', error)
        this.error = 'Не удалось загрузить материалы курса'
      } finally {
        this.loading = false
      }
    },
    async checkVideoForBlocks() {
      // Check video for each block in parallel
      const videoChecks = this.blocks.map(async (block) => {
        try {
          const response = await blocksApi.getBlockVideoInfo(block.id)
          if (response.data && response.data.status === 'READY') {
            block.hasVideo = true
            block.videoInfo = response.data
          }
        } catch {
          // No video for this block - that's expected
          block.hasVideo = false
        }
      })
      await Promise.all(videoChecks)
    },
    getBlockImageUrl(blockId: number) {
      return blocksApi.getBlockImageUrl(blockId)
    },
    getVideoDuration(block: BlockWithMedia): string {
      return block.videoInfo?.formattedDuration || ''
    },
    // Check if block is a pure test block (has test but no video)
    isPureTestBlock(block: BlockWithMedia): boolean {
      return block.isTestBlock === true && !block.hasVideo
    }
  }
}
</script>

<template>
  <div class="course-container">
    <div class="course-header">
      <h1 class="course-title">Основы ремонта техники</h1>
      <p class="course-description">
        Научитесь ремонтировать технику, используя отвертку, руки и силу мыслей
      </p>
      <div class="course-meta">
        <div class="meta-tags">
          <div class="meta-tag">
            <span>{{ blocksCount }} уроков</span>
          </div>
          <div class="meta-tag">
            <span>Начальный</span>
          </div>
        </div>
        <div class="instructor-info">
          <img
              src="https://sun1-85.userapi.com/s/v1/ig2/JquCXZGJElIMQC952QDJEeZfTBDgu5OdiROVOHKdguWo_-LFtxR09nGwdXhMecxOvSH2y6bTIsxvDyi8NR8PGDF1.jpg?quality=95&crop=392,386,328,328&as=32x32,48x48,72x72,108x108,160x160,240x240&ava=1&u=Kx6FRuRqcgp0kBMoK6rQAqva5ZjWDneWjG7NIeBuqns&cs=400x400"
              alt="Преподаватель" class="instructor-avatar">
          <div class="instructor-details">
            <h4 class="instructor-name">Вершинин Максим</h4>
            <p class="instructor-description">Старший разработчик, гений и миллиардер.</p>
          </div>
        </div>
      </div>
    </div>

    <div class="course-content">
      <section class="content-section">
        <h2 class="section-title">Описание</h2>
        <p>
          Этот курс предназначен для тех, кто только начинает свой путь в ремонте. Каждый модуль содержит практические
          задания для закрепления материала.
        </p>
      </section>

      <section class="content-section">
        <div class="section-header" @click="toggleMaterials">
          <h2 class="section-title">Материалы курса ({{ blocksCount }})</h2>
          <div class="toggle-icon">{{ isMaterialVisible ? '▼' : '▶' }}</div>
        </div>

        <div v-if="loading" class="loading-state">
          Загрузка материалов...
        </div>

        <div v-else-if="error" class="error-state">
          {{ error }}
        </div>

        <div v-else-if="blocks.length === 0" class="empty-state">
          Материалы курса пока не добавлены.
        </div>

        <div v-else v-show="isMaterialVisible" class="material-list">
          <router-link 
            v-for="block in blocks" 
            :key="block.id"
            :to="{ name: 'material', params: { id: String(block.id) } }"
            class="material-item"
          >
            <!-- Test block preview -->
            <div v-if="isPureTestBlock(block)" class="item-preview test-preview">
              <div class="test-icon-large">📋</div>
              <span class="test-label">Тест</span>
            </div>
            <!-- Video/Image block preview -->
            <div v-else class="item-preview" :style="{ backgroundImage: `url(${getBlockImageUrl(block.id)})` }">
              <span v-if="block.hasVideo" class="video-indicator">
                <span class="play-icon">▶</span>
                <span v-if="getVideoDuration(block)" class="video-duration">{{ getVideoDuration(block) }}</span>
              </span>
            </div>
            <h5 class="item-header">{{ block.title }}</h5>
            <p class="item-status" :class="{ available: block.isAvailable }">
              {{ block.isAvailable ? 'Доступен' : 'Недоступен' }}
              <span v-if="block.hasVideo" class="video-badge">Видео</span>
              <span v-if="isPureTestBlock(block)" class="test-badge">Тест</span>
            </p>
          </router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.course-container {
  padding: 0 100px;
  width: 100%;
}

.meta-tag {
  display: inline-block;
  border: #f1f1f1 solid 1px;
  border-radius: 15px;
  padding: 5px 20px;
  height: 20px;
}

.content-section {
  margin: 10px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 10px 0;
  border-bottom: 0.5px solid #e0e0e0;
  user-select: none;
}

.toggle-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.material-list {
  display: grid;
  margin: 20px 0;
  grid-template-columns: repeat(auto-fill, 300px);
  gap: 20px;
  justify-content: center;
}

.material-item {
  height: 270px;
  border: 1px solid #e0e0e0;
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.3s;
  text-decoration: none;
  color: inherit;
  display: block;
}

.material-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.item-preview {
  position: relative;
  background-color: #f5f5f5;
  height: 200px;
  width: 300px;
  background-size: cover;
  background-position: center;
  border-bottom: 1px solid #e0e0e0;
}

.item-order {
  position: absolute;
  left: 10px;
  top: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Test block preview styles */
.test-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.test-icon-large {
  font-size: 64px;
  margin-bottom: 8px;
}

.test-label {
  color: white;
  font-size: 18px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.test-badge {
  display: inline-block;
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  margin-left: 8px;
  font-weight: 500;
}

.item-header {
  padding: 0 10px;
  margin: 10px 0;
}

.item-status {
  padding: 0 10px;
  margin: 0;
  font-size: 12px;
  color: #999;
}

.item-status.available {
  color: #2e7d32;
}

.video-indicator {
  position: absolute;
  right: 10px;
  bottom: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 6px 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
}

.play-icon {
  font-size: 10px;
}

.video-duration {
  font-size: 11px;
}

.video-badge {
  display: inline-block;
  background: #007bff;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  margin-left: 8px;
  font-weight: 500;
}

.loading-state,
.error-state,
.empty-state {
  padding: 40px;
  text-align: center;
  color: #666;
  font-size: 16px;
}

.error-state {
  color: #c62828;
  background: #ffebee;
  border-radius: 8px;
  margin: 20px 0;
}

.course-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.instructor-info {
  display: flex;
  align-items: center;
  gap: 25px;
}

.instructor-name {
  margin: 5px 0;
}

.instructor-description {
  margin: 0;
}

.instructor-avatar {
  width: 50px;
  height: 50px;
  border-radius: 25px;
}
</style>