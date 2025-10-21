import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from './router/router.js'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'vant/lib/index.css';
import Vant from 'vant'
const app = createApp(App)
app.use(ElementPlus,{locale: zhCn,}, { size: 'small', zIndex: 3000 })
app.use(router)
app.use(Vant)
app.mount('#app')