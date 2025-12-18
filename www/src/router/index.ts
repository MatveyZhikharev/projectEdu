import { createRouter, createWebHistory } from 'vue-router'
import AboutView from "@/views/AboutView.vue"
import AdminView from "@/views/AdminVue.vue"
import HomeView from '@/views/HomeView.vue'
import MyCoursesView from '@/views/MyCoursesView.vue'
import CatalogView from '@/views/CatalogView.vue'
import PromotionsView from '@/views/PromotionsView.vue'
import ProfileView from '@/views/ProfileView.vue'
import MaterialView from '@/views/MaterialView.vue'
import HelpView from "@/views/HelpView.vue"
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
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
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/my-courses',
      name: 'my-courses',
      component: MyCoursesView,
      meta: { requiresAuth: true }
    },
    {
      path: '/catalog',
      name: 'catalog',
      component: CatalogView,
      meta: { requiresAuth: true }
    },
    {
      path: '/help',
      name: 'help',
      component: HelpView,
    },
    {
      path: '/promotions',
      name: 'promotions',
      component: PromotionsView,
      meta: { requiresAuth: true }
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
      meta: { requiresAuth: true }
    },
    {
      path: '/api/auth/vkCallback',
      name: 'auth.vkCallback',
      component: ProfileView,
    },
  ],
})

// Navigation guard to redirect unauthenticated users to profile page
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  
  // Initialize from storage if not already done
  if (!userStore.isAuthenticated && !userStore.loading) {
    userStore.initFromStorage()
  }
  
  // Check if route requires authentication
  if (to.meta.requiresAuth) {
    // If not authenticated and not loading, check with API
    if (!userStore.isAuthenticated) {
      try {
        await userStore.checkAuthStatus()
      } catch {
        // API error - redirect to profile
      }
    }
    
    // After checking, redirect to profile if still not authenticated
    if (!userStore.isAuthenticated) {
      next({ name: 'profile' })
      return
    }
    
    // Check admin requirement
    if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
      next({ name: 'home' })
      return
    }
  }
  
  next()
})

export default router
