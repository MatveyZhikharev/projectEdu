<script lang="ts">
import { blocksApi, type BlockResponse } from '@/api/blocks'

interface MaterialItem {
  id: number
  title: string
  description: string
  preview: string
  duration: string
}

export default {
  data() {
    return {
      isMaterialVisible: false,
      loading: false,
      error: null as string | null,
      blocks: [] as BlockResponse[],
      materials: [
        {
          id: 1,
          title: "Знакомство",
          description: "Вводный урок по ремонту техники",
          preview: "",
          duration: "48:52"
        },
        {
          id: 2,
          title: "Знакомство",
          description: "",
          preview: "",
          duration: ""
        },
        {
          id: 3,
          title: "Знакомство",
          description: "",
          preview: "",
          duration: ""
        },
        {
          id: 4,
          title: "Знакомство",
          description: "",
          preview: "",
          duration: ""
        }
      ] as MaterialItem[]
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
        // Обновляем материалы на основе блоков из API
        if (this.blocks.length > 0) {
          this.materials = this.blocks.map(block => ({
            id: block.id,
            title: block.title,
            description: '',
            preview: blocksApi.getBlockImageUrl(block.id),
            duration: ''
          }))
        }
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
            <span>24 часа</span>
          </div>
          <div class="meta-tag">
            <span>Начальный</span>
          </div>
          <div class="meta-tag">
            <span>1,245 студентов</span>
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
          задания
          для закрепления материала.
        </p>
      </section>

      <section class="content-section">
        <div class="section-header" @click="toggleMaterials">
          <h2 class="section-title">Материалы курса</h2>
          <div class="toggle-icon">{{ isMaterialVisible ? '▼' : '▶' }}</div>
        </div>
        <div v-show="isMaterialVisible" class="material-list">
          <div class="material-item" v-for="material in materials" :key="material.id">
            <div class="item-preview">
              <span class="item-duration">{{ material.duration }}</span>
            </div>
            <h5 class="item-header">{{ material.title }}</h5>
            <p class="item-description">{{ material.description }}</p>
          </div>
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
}

.item-preview {
  position: relative;
  background-image: url(https://avatars.mds.yandex.net/i?id=57b6f6ee1e1c40386ef66bc8a18f6b19e079f0ef-4103093-images-thumbs&n=13);
  height: 200px;
  width: 300px;
  background-size: cover;
  border-bottom: 1px solid #e0e0e0;
}

.item-duration {
  position: absolute;
  right: 10px;
  bottom: 10px;
}

.item-header {
  padding: 0 10px;
  margin: 10px 0;
}

.item-description {
  padding: 0 10px;
  margin: 0;
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