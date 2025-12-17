<script lang="ts">
import {useUserStore} from "@/stores/user.ts";

export default {
  computed: {
    userStore() {
      return useUserStore()
    },
  }
}
</script>

<template>
  <header>
    <div class="logo menu-logo">
      <img src="../assets/logo.svg" alt="" width="150px">
    </div>
    <nav class="header-nav">
      <RouterLink to="help" class="menu-link">
        <div class="header-link">Помощь</div>
      </RouterLink>
      <RouterLink to="about" class="menu-link">
        <div class="header-link">О нас</div>
      </RouterLink>
    </nav>
    <div class="auth-section">
      <div class="profile-avatar">
        <span class="avatar-icon">👤</span>
      </div>
      &nbsp;&nbsp;&nbsp;
      <span>{{ userStore.displayName }}</span>
    </div>
  </header>
</template>

<style scoped>
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 100px;
  box-shadow: 0px 10px 25px #f1f1f1;
}

.header-nav {
  display: flex;
  gap: 200px;
}

.header-link {
  font-size: 16px;
  font-family: 'Roboto Mono', Tahoma, Geneva, Verdana, sans-serif;
}

.menu-logo {
  /* margin: 30px 90px 50px 60px; */
}

.search-input {
  font-size: 16px;
  border: #a0a0a0 solid 1px;
  padding: 10px 25px;
  border-radius: 10px;
}

.auth-section {
  display: flex;
  align-items: center;
}

.auth-icon {
  font-size: 24px;
}

.profile-avatar {
  width: 35px;
  height: 35px;
  background: rgba(135, 144, 227, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>