import {createApp} from 'vue'
import './style.css'
import App from './App.vue'
import Antd from "ant-design-vue";

const app = createApp(App)
app.use(Antd)
app.mount('#app')

