import { createApp } from 'vue'
import App from './App.vue'
import * as VueRouter from 'vue-router'
import routes from "./config/route.ts"
import './global.css'

const app = createApp(App);
// 创建路由实例并传递 `routes` 配置
const router = VueRouter.createRouter({
    //history: VueRouter.createWebHashHistory(),
    history: VueRouter.createWebHistory(),
    routes,
})
app.use(router);
app.mount('#app');
