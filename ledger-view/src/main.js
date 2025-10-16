import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from './router/router.js'
import 'element-plus/dist/index.css'
import 'vant/lib/index.css';
import Vant from 'vant'
//createApp(App).mount('#app')
const app = createApp(App)
app.use(ElementPlus, { size: 'small', zIndex: 3000 })
app.use(router)
app.use(Vant)
app.mount('#app')