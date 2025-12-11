<script lang="ts">
import { blocksAdminApi, type BlockAddRequest, type BlockUpdateRequest } from '@/api/blocksAdmin'
import { usersAdminApi, type User, type UserUpdateDto, type PageResponse } from '@/api/usersAdmin'
import { videoApi, type VideoDTO } from '@/api/video'
import type { BlockResponse } from '@/api/blocks'

interface BlockMaterial {
  id?: number
  title: string
  sortOrder?: number
  isAvailable?: boolean
}

export default {
  data() {
    return {
      activeTab: 'blocks' as 'blocks' | 'users' | 'videos',
      course: {
        title: "Основы ремонта техники",
        description: "Научитесь ремонтировать технику, используя отвертку, руки и силу мыслей",
        duration: "24 часа",
        level: "Начальный",
        studentsCount: 1245
      },
      blocks: [] as BlockResponse[],
      loading: false,
      error: null as string | null,
      newBlock: {
        title: ""
      },
      editingBlockId: null as number | null,
      editingBlockTitle: "",
      // User management
      users: [] as User[],
      usersLoading: false,
      usersError: null as string | null,
      usersPagination: {
        page: 0,
        size: 10,
        totalPages: 0,
        totalElements: 0
      },
      editingUserId: null as string | null,
      editingUserData: {} as UserUpdateDto,
      // Video management
      videos: [] as VideoDTO[],
      videosLoading: false,
      videosError: null as string | null,
      newVideo: {
        title: "",
        description: "",
        file: null as File | null
      },
      uploadingVideo: false
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
        const response = await blocksAdminApi.getAllBlocks()
        this.blocks = response.data
      } catch (error: any) {
        console.error('Ошибка загрузки блоков:', error)
        this.error = 'Не удалось загрузить блоки'
      } finally {
        this.loading = false
      }
    },
    async addBlock() {
      if (!this.newBlock.title.trim()) return
      
      try {
        const request: BlockAddRequest = { title: this.newBlock.title }
        await blocksAdminApi.createBlock(request)
        this.newBlock.title = ""
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка создания блока:', error)
        this.error = 'Не удалось создать блок'
      }
    },
    async deleteBlock(blockId: number) {
      if (!confirm('Удалить этот блок?')) return
      
      try {
        await blocksAdminApi.deleteBlock(blockId)
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка удаления блока:', error)
        this.error = 'Не удалось удалить блок'
      }
    },
    async toggleBlockStatus(blockId: number) {
      try {
        await blocksAdminApi.toggleBlockStatus(blockId)
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка изменения статуса:', error)
        this.error = 'Не удалось изменить статус блока'
      }
    },
    async swapBlocks(firstBlockId: number, secondBlockId: number) {
      try {
        await blocksAdminApi.swapBlocks(firstBlockId, secondBlockId)
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка перемещения блоков:', error)
        this.error = 'Не удалось поменять блоки местами'
      }
    },
    startEditingBlock(block: BlockResponse) {
      this.editingBlockId = block.id
      this.editingBlockTitle = block.title
    },
    cancelEditingBlock() {
      this.editingBlockId = null
      this.editingBlockTitle = ""
    },
    async saveBlockEdit() {
      if (!this.editingBlockId || !this.editingBlockTitle.trim()) return
      
      try {
        const request: BlockUpdateRequest = {
          blockId: this.editingBlockId,
          title: this.editingBlockTitle
        }
        await blocksAdminApi.updateBlock(request)
        this.cancelEditingBlock()
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка обновления блока:', error)
        this.error = 'Не удалось обновить блок'
      }
    },
    async handleImageUpload(event: Event, blockId: number) {
      const target = event.target as HTMLInputElement
      const file = target.files?.[0]
      if (!file) return
      
      try {
        await blocksAdminApi.updateBlockImage(blockId, file)
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка загрузки изображения:', error)
        this.error = 'Не удалось загрузить изображение'
      }
    },
    async handleVideoUpload(event: Event, blockId: number) {
      const target = event.target as HTMLInputElement
      const file = target.files?.[0]
      if (!file) return
      
      try {
        await blocksAdminApi.updateBlockVideo(blockId, file)
        await this.fetchBlocks()
      } catch (error: any) {
        console.error('Ошибка загрузки видео:', error)
        this.error = 'Не удалось загрузить видео'
      }
    },
    moveBlockUp(index: number) {
      if (index === 0) return
      const currentBlock = this.blocks[index]
      const prevBlock = this.blocks[index - 1]
      if (currentBlock && prevBlock) {
        this.swapBlocks(currentBlock.id, prevBlock.id)
      }
    },
    moveBlockDown(index: number) {
      if (index === this.blocks.length - 1) return
      const currentBlock = this.blocks[index]
      const nextBlock = this.blocks[index + 1]
      if (currentBlock && nextBlock) {
        this.swapBlocks(currentBlock.id, nextBlock.id)
      }
    },
    // User management methods
    async fetchUsers() {
      this.usersLoading = true
      this.usersError = null
      try {
        const response = await usersAdminApi.getUsers(this.usersPagination.page, this.usersPagination.size)
        const data = response.data
        this.users = data.content
        this.usersPagination.totalPages = data.totalPages
        this.usersPagination.totalElements = data.totalElements
      } catch (error: any) {
        console.error('Ошибка загрузки пользователей:', error)
        this.usersError = 'Не удалось загрузить пользователей'
      } finally {
        this.usersLoading = false
      }
    },
    async switchTab(tab: 'blocks' | 'users' | 'videos') {
      this.activeTab = tab
      if (tab === 'users' && this.users.length === 0) {
        await this.fetchUsers()
      }
      if (tab === 'videos' && this.videos.length === 0) {
        await this.fetchVideos()
      }
    },
    startEditingUser(user: User) {
      this.editingUserId = user.id
      this.editingUserData = {
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        status: user.status,
        role: user.role
      }
    },
    cancelEditingUser() {
      this.editingUserId = null
      this.editingUserData = {}
    },
    async saveUserEdit() {
      if (!this.editingUserId) return
      
      try {
        await usersAdminApi.updateUser(this.editingUserId, this.editingUserData)
        this.cancelEditingUser()
        await this.fetchUsers()
      } catch (error: any) {
        console.error('Ошибка обновления пользователя:', error)
        this.usersError = 'Не удалось обновить пользователя'
      }
    },
    async goToUserPage(page: number) {
      if (page < 0 || page >= this.usersPagination.totalPages) return
      this.usersPagination.page = page
      await this.fetchUsers()
    },
    getStatusLabel(status: string) {
      const labels: Record<string, string> = {
        REGISTERED: 'Зарегистрирован',
        ACTIVE: 'Активен',
        BLOCKED: 'Заблокирован'
      }
      return labels[status] || status
    },
    getRoleLabel(role: string) {
      const labels: Record<string, string> = {
        USER: 'Пользователь',
        ADMIN: 'Администратор'
      }
      return labels[role] || role
    },
    // Video management methods
    async fetchVideos() {
      this.videosLoading = true
      this.videosError = null
      try {
        const response = await videoApi.getAllVideos()
        this.videos = response.data
      } catch (error: any) {
        console.error('Ошибка загрузки видео:', error)
        this.videosError = 'Не удалось загрузить видео'
      } finally {
        this.videosLoading = false
      }
    },
    handleVideoFileSelect(event: Event) {
      const target = event.target as HTMLInputElement
      const file = target.files?.[0]
      if (file) {
        this.newVideo.file = file
      }
    },
    async uploadNewVideo() {
      if (!this.newVideo.file || !this.newVideo.title.trim()) return
      
      this.uploadingVideo = true
      try {
        await videoApi.uploadVideo(
          this.newVideo.file,
          this.newVideo.title,
          this.newVideo.description || undefined
        )
        this.newVideo = { title: "", description: "", file: null }
        await this.fetchVideos()
      } catch (error: any) {
        console.error('Ошибка загрузки видео:', error)
        this.videosError = 'Не удалось загрузить видео'
      } finally {
        this.uploadingVideo = false
      }
    },
    getVideoStreamUrl(videoId: number) {
      return videoApi.getVideoStreamUrl(videoId)
    },
    formatDate(dateString: string) {
      return new Date(dateString).toLocaleDateString('ru-RU', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    formatDuration(seconds: number | null) {
      if (!seconds) return 'Неизвестно'
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    getVideoStatusLabel(status: string) {
      const labels: Record<string, string> = {
        READY: 'Готово',
        PROCESSING: 'Обработка',
        ERROR: 'Ошибка'
      }
      return labels[status] || status
    }
  }
}
</script>

<template>
  <div class="admin-container">
    <!-- Tabs -->
    <div class="admin-tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'blocks' }]"
        @click="switchTab('blocks')"
      >
        Управление блоками
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'users' }]"
        @click="switchTab('users')"
      >
        Управление пользователями
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'videos' }]"
        @click="switchTab('videos')"
      >
        Управление видео
      </button>
    </div>

    <!-- Error message -->
    <div v-if="error || usersError || videosError" class="error-message">
      {{ error || usersError || videosError }}
    </div>

    <!-- Blocks Tab -->
    <div v-if="activeTab === 'blocks'" class="blocks-section">
      <div class="course-header">
        <h1 class="course-title">{{ course.title }}</h1>
        <p class="course-description">{{ course.description }}</p>

        <div class="course-meta">
          <div class="meta-tags">
            <div class="meta-tag">
              <span>{{ course.duration }}</span>
            </div>
            <div class="meta-tag">
              <span>{{ course.level }}</span>
            </div>
            <div class="meta-tag">
              <span>{{ course.studentsCount }} студентов</span>
            </div>
          </div>
        </div>
      </div>

      <div class="course-content">
        <section class="content-section">
          <div class="section-header">
            <h2 class="section-title">Блоки курса</h2>
          </div>

          <!-- Add new block form -->
          <div class="add-block-form">
            <h3>Добавить новый блок</h3>
            <div class="form-row">
              <input 
                v-model="newBlock.title" 
                placeholder="Название блока" 
                class="block-input"
                @keyup.enter="addBlock"
              >
              <button @click="addBlock" class="add-btn">Добавить</button>
            </div>
          </div>

          <!-- Loading state -->
          <div v-if="loading" class="loading">
            Загрузка блоков...
          </div>

          <!-- Blocks list -->
          <div v-else class="blocks-list">
            <div 
              v-for="(block, index) in blocks" 
              :key="block.id"
              class="block-item"
            >
              <div class="block-order">
                <button 
                  @click="moveBlockUp(index)" 
                  :disabled="index === 0"
                  class="order-btn"
                >↑</button>
                <span class="order-number">{{ block.sortOrder }}</span>
                <button 
                  @click="moveBlockDown(index)" 
                  :disabled="index === blocks.length - 1"
                  class="order-btn"
                >↓</button>
              </div>

              <div class="block-info">
                <div v-if="editingBlockId === block.id" class="edit-block-form">
                  <input 
                    v-model="editingBlockTitle" 
                    class="edit-input"
                    @keyup.enter="saveBlockEdit"
                    @keyup.escape="cancelEditingBlock"
                  >
                  <div class="edit-actions">
                    <button @click="saveBlockEdit" class="save-btn-sm">Сохранить</button>
                    <button @click="cancelEditingBlock" class="cancel-btn">Отмена</button>
                  </div>
                </div>
                <div v-else>
                  <h4 class="block-title">{{ block.title }}</h4>
                  <div class="block-status" :class="{ available: block.isAvailable }">
                    {{ block.isAvailable ? 'Опубликован' : 'Черновик' }}
                  </div>
                </div>
              </div>

              <div class="block-actions">
                <button @click="startEditingBlock(block)" class="action-btn edit">
                  ✏️
                </button>
                <button @click="toggleBlockStatus(block.id)" class="action-btn status">
                  {{ block.isAvailable ? '🔒' : '🔓' }}
                </button>
                <label class="action-btn upload">
                  🖼️
                  <input 
                    type="file" 
                    accept="image/*" 
                    @change="handleImageUpload($event, block.id)"
                    hidden
                  >
                </label>
                <label class="action-btn upload">
                  🎬
                  <input 
                    type="file" 
                    accept="video/*" 
                    @change="handleVideoUpload($event, block.id)"
                    hidden
                  >
                </label>
                <button @click="deleteBlock(block.id)" class="action-btn delete">
                  🗑️
                </button>
              </div>
            </div>

            <div v-if="blocks.length === 0 && !loading" class="empty-state">
              Нет блоков. Добавьте первый блок!
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- Users Tab -->
    <div v-if="activeTab === 'users'" class="users-section">
      <div class="section-header">
        <h2 class="section-title">Управление пользователями</h2>
      </div>

      <!-- Loading state -->
      <div v-if="usersLoading" class="loading">
        Загрузка пользователей...
      </div>

      <!-- Users table -->
      <div v-else class="users-table-container">
        <table class="users-table">
          <thead>
            <tr>
              <th>Имя</th>
              <th>Email</th>
              <th>Статус</th>
              <th>Роль</th>
              <th>Дата регистрации</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <template v-if="editingUserId === user.id">
                <td>
                  <input v-model="editingUserData.firstName" class="table-input" placeholder="Имя">
                  <input v-model="editingUserData.lastName" class="table-input" placeholder="Фамилия">
                </td>
                <td>
                  <input v-model="editingUserData.email" class="table-input" placeholder="Email">
                </td>
                <td>
                  <select v-model="editingUserData.status" class="table-select">
                    <option value="REGISTERED">Зарегистрирован</option>
                    <option value="ACTIVE">Активен</option>
                    <option value="BLOCKED">Заблокирован</option>
                  </select>
                </td>
                <td>
                  <select v-model="editingUserData.role" class="table-select">
                    <option value="USER">Пользователь</option>
                    <option value="ADMIN">Администратор</option>
                  </select>
                </td>
                <td>{{ new Date(user.registrationDate).toLocaleDateString('ru-RU') }}</td>
                <td>
                  <button @click="saveUserEdit" class="save-btn-sm">Сохранить</button>
                  <button @click="cancelEditingUser" class="cancel-btn">Отмена</button>
                </td>
              </template>
              <template v-else>
                <td>{{ user.firstName }} {{ user.lastName }}</td>
                <td>{{ user.email }}</td>
                <td>
                  <span :class="['status-badge', user.status.toLowerCase()]">
                    {{ getStatusLabel(user.status) }}
                  </span>
                </td>
                <td>
                  <span :class="['role-badge', user.role.toLowerCase()]">
                    {{ getRoleLabel(user.role) }}
                  </span>
                </td>
                <td>{{ new Date(user.registrationDate).toLocaleDateString('ru-RU') }}</td>
                <td>
                  <button @click="startEditingUser(user)" class="action-btn edit">✏️</button>
                </td>
              </template>
            </tr>
          </tbody>
        </table>

        <div v-if="users.length === 0 && !usersLoading" class="empty-state">
          Нет пользователей.
        </div>

        <!-- Pagination -->
        <div v-if="usersPagination.totalPages > 1" class="pagination">
          <button 
            @click="goToUserPage(usersPagination.page - 1)" 
            :disabled="usersPagination.page === 0"
            class="page-btn"
          >
            ← Назад
          </button>
          <span class="page-info">
            Страница {{ usersPagination.page + 1 }} из {{ usersPagination.totalPages }}
          </span>
          <button 
            @click="goToUserPage(usersPagination.page + 1)" 
            :disabled="usersPagination.page >= usersPagination.totalPages - 1"
            class="page-btn"
          >
            Вперед →
          </button>
        </div>
      </div>
    </div>

    <!-- Videos Tab -->
    <div v-if="activeTab === 'videos'" class="videos-section">
      <div class="section-header">
        <h2 class="section-title">Управление видео</h2>
      </div>

      <!-- Upload new video form -->
      <div class="add-block-form">
        <h3>Загрузить новое видео</h3>
        <div class="form-row">
          <input 
            v-model="newVideo.title" 
            placeholder="Название видео" 
            class="block-input"
          >
          <input 
            v-model="newVideo.description" 
            placeholder="Описание (опционально)" 
            class="block-input"
          >
        </div>
        <div class="form-row" style="margin-top: 10px;">
          <input 
            type="file" 
            accept="video/*" 
            @change="handleVideoFileSelect"
            class="file-input"
          >
          <button 
            @click="uploadNewVideo" 
            :disabled="uploadingVideo || !newVideo.file || !newVideo.title.trim()"
            class="add-btn"
          >
            {{ uploadingVideo ? 'Загрузка...' : 'Загрузить' }}
          </button>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="videosLoading" class="loading">
        Загрузка видео...
      </div>

      <!-- Videos list -->
      <div v-else class="videos-list">
        <div 
          v-for="video in videos" 
          :key="video.id"
          class="video-item"
        >
          <div class="video-preview">
            <span class="video-icon">🎬</span>
          </div>
          <div class="video-info">
            <h4 class="video-title">{{ video.title }}</h4>
            <p v-if="video.description" class="video-description">{{ video.description }}</p>
            <div class="video-meta">
              <span class="video-status" :class="video.status.toLowerCase()">
                {{ getVideoStatusLabel(video.status) }}
              </span>
              <span class="video-duration" v-if="video.duration">
                {{ formatDuration(video.duration) }}
              </span>
              <span class="video-date">
                {{ formatDate(video.createdAt) }}
              </span>
            </div>
          </div>
          <div class="video-actions">
            <a 
              :href="getVideoStreamUrl(video.id)" 
              target="_blank" 
              class="action-btn play"
              title="Смотреть видео"
            >
              ▶️
            </a>
          </div>
        </div>

        <div v-if="videos.length === 0 && !videosLoading" class="empty-state">
          Нет загруженных видео. Загрузите первое видео!
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-container {
  padding: 20px 100px;
  width: 100%;
}

/* Tabs */
.admin-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  background: #f5f5f5;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #007bff;
  color: white;
}

.tab-btn:hover:not(.active) {
  background: #e0e0e0;
}

/* Error message */
.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

/* Course header */
.course-header {
  margin-bottom: 30px;
}

.course-title {
  font-size: 28px;
  margin: 0 0 10px 0;
}

.course-description {
  color: #666;
  margin: 0 0 20px 0;
}

.meta-tag {
  display: inline-block;
  border: #e0e0e0 solid 1px;
  border-radius: 15px;
  padding: 5px 20px;
  margin-right: 10px;
  font-size: 14px;
}

.meta-tags {
  margin-bottom: 20px;
}

/* Content sections */
.content-section {
  margin: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 22px;
}

/* Add block form */
.add-block-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.add-block-form h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
}

.form-row {
  display: flex;
  gap: 10px;
}

.block-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.add-btn {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.add-btn:hover {
  background: #138496;
}

/* Loading */
.loading {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 16px;
}

/* Blocks list */
.blocks-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.block-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  transition: box-shadow 0.3s;
}

.block-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.block-order {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.order-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.order-btn:hover:not(:disabled) {
  background: #f0f0f0;
}

.order-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.order-number {
  font-weight: 600;
  color: #666;
}

.block-info {
  flex: 1;
}

.block-title {
  margin: 0 0 6px 0;
  font-size: 16px;
}

.block-status {
  font-size: 12px;
  color: #999;
  padding: 4px 10px;
  background: #f5f5f5;
  border-radius: 12px;
  display: inline-block;
}

.block-status.available {
  background: #e8f5e9;
  color: #2e7d32;
}

.edit-block-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.edit-input {
  padding: 8px 12px;
  border: 1px solid #007bff;
  border-radius: 6px;
  font-size: 14px;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.block-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #f5f5f5;
}

.action-btn.delete:hover {
  background: #ffebee;
  border-color: #ef5350;
}

.action-btn.upload {
  cursor: pointer;
}

.save-btn-sm {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.cancel-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  background: #f9f9f9;
  border-radius: 10px;
}

/* Users section */
.users-section {
  margin-top: 20px;
}

.users-table-container {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.users-table th,
.users-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.users-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.users-table tr:hover {
  background: #f9f9f9;
}

.table-input {
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  width: 100%;
  margin-bottom: 4px;
}

.table-select {
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  width: 100%;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.registered {
  background: #fff3e0;
  color: #e65100;
}

.status-badge.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge.blocked {
  background: #ffebee;
  color: #c62828;
}

.role-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.user {
  background: #e3f2fd;
  color: #1565c0;
}

.role-badge.admin {
  background: #f3e5f5;
  color: #7b1fa2;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 20px;
}

.page-btn {
  padding: 10px 20px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

/* Videos section */
.videos-section {
  margin-top: 20px;
}

.videos-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  transition: box-shadow 0.3s;
}

.video-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.video-preview {
  width: 80px;
  height: 60px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.video-info {
  flex: 1;
}

.video-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
}

.video-description {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
}

.video-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.video-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.video-status.ready {
  background: #e8f5e9;
  color: #2e7d32;
}

.video-status.processing {
  background: #fff3e0;
  color: #e65100;
}

.video-status.error {
  background: #ffebee;
  color: #c62828;
}

.video-duration {
  font-size: 13px;
  color: #666;
}

.video-date {
  font-size: 12px;
  color: #999;
}

.video-actions {
  display: flex;
  gap: 8px;
}

.action-btn.play {
  background: #e3f2fd;
  border-color: #1976d2;
}

.action-btn.play:hover {
  background: #bbdefb;
}

.file-input {
  flex: 1;
  padding: 10px;
  border: 1px dashed #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
}
</style>