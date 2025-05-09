// 创建Axios实例
import axios from "axios";
import {message} from 'ant-design-vue';

export interface R<T> {
    data: T;
    message: string | null;
    success: boolean;
}

const axiosInstance = axios.create({
    timeout: 5000,
    headers: {
        'Content-Type': 'application/json',
    }
});

const url = new URL(window.location.href);
const params = new URLSearchParams(url.search);

export function getToken(): string {
    return <string>params.get("_token");
}

// 请求拦截器
axiosInstance.interceptors.request.use(
    config => {
        config.headers.token = getToken();
        return config;
    },
    error => {
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);

axiosInstance.interceptors.response.use(
    response => {
        if ('message' in response.data && 'data' in response.data) {
            let data: R<any> = response.data;
            if (!data.success) {
                message.error(data.message);
                return Promise.reject(data.message);
            }
        }
        return response.data;
    },
    error => {
        if (error.status === 401) {
            message.error("登录已过期，请重新登录");
        } else {
            message.error("server error:" + error.message);
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
