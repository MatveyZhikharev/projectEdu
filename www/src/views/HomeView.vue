<script lang="ts">
import { blocksApi, type BlockResponse } from '@/api/blocks'

export default {
  data() {
    return {
      isMaterialVisible: true,
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
    toggleMaterials() {
      this.isMaterialVisible = !this.isMaterialVisible
    },
    async fetchBlocks() {
      this.loading = true
      this.error = null
      try {
        const response = await blocksApi.getAllAvailableBlocks()
        this.blocks = response.data
      } catch (error: any) {
        console.error('Ошибка загрузки блоков:', error)
        this.error = 'Не удалось загрузить материалы курса'
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
        
        <!-- Loading state -->
        <div v-if="loading" class="loading-state">
          Загрузка материалов...
        </div>
        
        <!-- Error state -->
        <div v-else-if="error" class="error-state">
          {{ error }}
        </div>
        
        <!-- Empty state -->
        <div v-else-if="blocks.length === 0" class="empty-state">
          Материалы курса пока не добавлены.
        </div>
        
        <!-- Blocks list -->
        <div v-else v-show="isMaterialVisible" class="material-list">
          <router-link 
            v-for="block in blocks" 
            :key="block.id"
            :to="{ name: 'material', params: { id: String(block.id) } }"
            class="material-item"
          >
            <div class="item-preview" :style="{ backgroundImage: `url(${getBlockImageUrl(block.id)})` }">
              <span class="item-order">{{ block.sortOrder }}</span>
            </div>
            <h5 class="item-header">{{ block.title }}</h5>
            <p class="item-status" :class="{ available: block.isAvailable }">
              {{ block.isAvailable ? 'Доступен' : 'Недоступен' }}
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