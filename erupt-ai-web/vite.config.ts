import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
    base: './',
    plugins: [
        vue(),
        vueDevTools(),
        {
            name: 'rename-index',
            enforce: 'post',
            generateBundle(_, bundle) {
                const indexHtml = bundle['index.html'];
                if (indexHtml) {
                    indexHtml.fileName = 'ai-chat.html'; // 修改为你想要的文件名
                }
            },
        }
    ],
    build: {
        outDir: '../erupt-ai/src/main/resources/static',
        sourcemap: false
    },
    server: {
        proxy: {
            // 字符串简写形式
            '/erupt-api': 'http://localhost:9999', // 所有以 /api 开头的请求都会被代理到 http://localhost:3000
        }
    }
})
