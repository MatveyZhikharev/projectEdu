import { createRouter, createWebHistory } from 'vue-router'
import AboutView from "@/views/AboutView.vue"
import AdminView from "@/views/AdminVue.vue"
import HomeView from '@/views/HomeView.vue'
import MyCoursesView from '@/views/MyCoursesView.vue'
import CatalogView from '@/views/CatalogView.vue'
import PromotionsView from '@/views/PromotionsView.vue'
import ProfileView from '@/views/ProfileView.vue'
import MaterialView from '@/views/MaterialView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
    },
    {
      path: '/my-courses',
      name: 'my-courses',
      component: MyCoursesView,
    },
    {
      path: '/catalog',
      name: 'catalog',
      component: CatalogView,
    },
    {
      path: '/promotions',
      name: 'promotions',
      component: PromotionsView,
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
    },
    {
      path: '/material/:id',
      name: 'material',
      component: MaterialView,
    },
  ],
})

export default router
