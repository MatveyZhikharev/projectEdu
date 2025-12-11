<script lang="ts">
import { RouterLink } from 'vue-router'

export default {
  components: {
    RouterLink
  },
  data() {
    return {
      isActive: false
    }
  },
  methods: {
    handleMouseEnter() {
      this.isActive = true
    },
    handleMouseLeave() {
      this.isActive = false
    }
  }
}

</script>

<template>
  <aside :class="{ active: isActive }" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
    <nav>
      <ul>
        <li class="btn section-type">
          <RouterLink to="/" class="menu-link">
            <img src="../assets/main-page.png" alt="" width="20px">
            <span class="btn-description">&nbsp;&nbsp;&nbsp;&nbsp;Главная</span>
          </RouterLink>
        </li>
        <li class="btn section-type">
          <RouterLink to="/my-courses" class="menu-link">
            <img src="../assets/folder.png" alt="" width="20px">
            <span class="btn-description">&nbsp;&nbsp;&nbsp;&nbsp;Мои курсы</span>
          </RouterLink>
        </li>
        <li class="btn section-type">
          <RouterLink to="/catalog" class="menu-link">
            <img src="../assets/catalog.png" alt="" width="20px">
            <span class="btn-description">&nbsp;&nbsp;&nbsp;&nbsp;Каталог курсов</span>
          </RouterLink>
        </li>
        <li class="btn section-type">
          <RouterLink to="/promotions" class="menu-link">
            <img src="../assets/sale.png" alt="" width="20px">
            <span class="btn-description">&nbsp;&nbsp;&nbsp;&nbsp;Акции</span>
          </RouterLink>
        </li>
        <li class="btn section-type">
          <RouterLink to="/profile" class="menu-link">
            <img src="../assets/profile.png" alt="" width="20px">
            <span class="btn-description">&nbsp;&nbsp;&nbsp;&nbsp;Профиль</span>
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<style scoped>
aside {
  position: fixed;
  margin-top: 10px;
  background-color: #ffffff;
  box-shadow: 10px 0px 25px #f1f1f1;
}

ul {
  padding: 0 15px;
  display: flex;
  gap: 30px;
  flex-direction: column;
  list-style-type: none;
}

li {
  display: flex;
  align-items: center;
}

.menu-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.btn-description {
  width: 0;
  opacity: 100%;
  overflow-x: hidden;
  text-wrap: nowrap;
  transition: width 0.5s;
}

.active nav ul li .btn-description {
  width: 100%!important;
  opacity: 100%;
}

ul {
  width: 20px;
  transition: width 0.5s;
}

.active nav ul {
  width: 160px!important;
}

</style>