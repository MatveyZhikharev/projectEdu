<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { authApi } from '@/api/auth'

const isAuthenticated = ref(false)
const loading = ref(true)

onMounted(async () => {
  try {
    const response = await authApi.getAuthStatus()
    isAuthenticated.value = response.data.status
  } catch (error) {
    isAuthenticated.value = false
  } finally {
    loading.value = false
  }
})

async function handleVkLogin() {
  try {
    const response = await authApi.getVkAuthUrl()
    window.location.href = response.data.url
  } catch (error) {
    console.error('Ошибка получения ссылки VK:', error)
  }
}
</script>

<template>
  <div class="profile-container">
    <div v-if="loading" class="loading">
      Загрузка...
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
    
    <div v-else class="placeholder-container">
      <div class="placeholder-content">
        <div class="placeholder-icon">👤</div>
        <h1 class="placeholder-title">Профиль</h1>
        <p class="placeholder-message">Совсем скоро этот раздел будет доступен...</p>
        <p class="placeholder-description">
          Здесь вы сможете просмотреть и отредактировать свои личные данные,
          настройки аккаунта и историю обучения.
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  width: 100%;
  min-height: 60vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.loading {
  font-size: 18px;
  color: #666;
}

.auth-section {
  display: flex;
  justify-content: center;
  align-items: center;
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

.placeholder-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.placeholder-content {
  text-align: center;
  max-width: 500px;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.placeholder-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.placeholder-message {
  font-size: 20px;
  color: #007bff;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.placeholder-description {
  font-size: 16px;
  color: #666;
  margin: 0;
  line-height: 1.6;
}
</style>
