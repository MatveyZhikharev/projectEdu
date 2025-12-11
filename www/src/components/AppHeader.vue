<script setup lang="ts">

</script>

<template>
  <header>
    <div class="logo menu-logo">
      <img src="../assets/logo.svg" alt="" width="150px">
    </div>
    <nav class="header-nav">
      <div class="header-link">Прогресс курса</div>
      <div class="header-link">Материалы</div>
      <div class="header-link">Обсуждения</div>
      <div class="header-link">Помощь</div>
    </nav>
    <div>
      <input type="text" name="query" id="search" placeholder="Поиск..." class="search-input">
    </div>
  </header>
</template>

<style scoped>
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 75px;
  box-shadow: 0px 10px 25px #f1f1f1;
}
.header-nav {
  display: flex;
  gap: 75px;
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
</style>