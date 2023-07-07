import Vue from "vue";
import axios from "axios";

import {Message, Notification} from "element-ui";
import {getToken} from '@/api/auth'

// 第三方插件
import "element-ui/lib/theme-chalk/index.css";

Vue.prototype.$axios = axios;
// 字体图标

const service = axios.create({
	baseURL: process.env.VUE_APP_BASE_API,
	timeout: 50000
});

service.defaults.withCredentials = true; // 让ajax携带cookie
service.interceptors.request.use(
	// 每次请求都自动携带Cookie
	config => {
		config.headers.token = getToken();
		//config.headers.token = 'XC2sUaBYYoOjPMhD1';//万能token，测试用
		return config;
	},
	// eslint-disable-next-line handle-callback-err
	error => {
		return Promise.reject(error);
	}
);

service.interceptors.response.use(
	res => {
		//状态不是200的进行提示
		if (res.status !== 200) {
			Notification.error({
				title: res.status,
				message: res.statusText
			})
			return Promise.reject(res.data.message)
		}
		// 状态为SUCCESS或者为空都是成功
		res.data.success = res.data.status === "SUCCESS" || !res.data.status;
		res.data.message = res.data.message || "操作成功";
		if(!res.data.success) {//
			Notification({
				title: res.data.status,
				message: res.data.message,
				type: "warning"
			});
			return res.data;
		}
		return res.data
	},
	// 拦截异常的响应
	err => {
		switch (err.response.status) {
			case 401:
				//MessageBox.alert("登陆已过期，请关闭当前窗口重新进入");
				Notification({
					title: "登陆已过期",
					message: "请重新登录!",
					type: "error"
				});
				break;
			case 403:
				Message.warning("抱歉，您无权访问！")
				break;
			case 500:
				Notification.error({ title: "操作失败", message: err.response.data.message });
				break;
			case 404:
				Notification({
					title: "404",
					message: "接口不存在",
					type: "warning"
				});
				break;
		}
		//throw 'request error'
		return Promise.reject(err);
	}
);

export default service;
