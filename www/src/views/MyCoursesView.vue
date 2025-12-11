<script setup lang="ts">
</script>

<template>
  <div class="placeholder-container">
    <div class="placeholder-content">
      <div class="placeholder-icon">📚</div>
      <h1 class="placeholder-title">Мои курсы</h1>
      <p class="placeholder-message">Совсем скоро этот раздел будет доступен...</p>
      <p class="placeholder-description">
        Здесь вы сможете видеть все курсы, на которые вы записались, 
        и отслеживать свой прогресс обучения.
      </p>
    </div>
  </div>
</template>

<style scoped>
.placeholder-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  width: 100%;
  padding: 40px;
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
