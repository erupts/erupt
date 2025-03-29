import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueDevTools(),
    ],
    server: {
        proxy: {
            // 字符串简写形式
            '/erupt-api': 'http://localhost:8080', // 所有以 /api 开头的请求都会被代理到 http://localhost:3000
        }
    }
})
