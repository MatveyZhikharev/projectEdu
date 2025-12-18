import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// Important: createPinia must be used before router (which uses useUserStore)
const pinia = createPinia()
app.use(pinia)
app.use(router)

app.mount('#app')