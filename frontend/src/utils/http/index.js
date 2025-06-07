import axios from 'axios';
import { message } from 'antd';

// Base configuration for axios
const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Request interceptor for API calls
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for API calls
http.interceptors.response.use(
  (response) => {
    // 从 Result<T> 中获取业务数据
    const { code, message: msg, data } = response.data;
    
    if (code === 200 || code === 0) {
      return data;
    } else {
      message.error(msg || '请求失败');
      return Promise.reject(new Error(msg || '请求失败'));
    }
  },
  (error) => {
    if (error.response) {
      const { status } = error.response;
      
      switch (status) {
        case 401:
          message.error('登录已过期，请重新登录');
          localStorage.removeItem('token');
          // 可以在这里执行重定向到登录页面
          window.location.href = '/login';
          break;
        case 403:
          message.error('没有权限访问此资源');
          break;
        case 500:
          message.error('服务器错误，请稍后再试');
          break;
        default:
          message.error(error.response.data.message || '未知错误');
      }
    } else if (error.request) {
      message.error('网络错误，请检查您的网络连接');
    } else {
      message.error('请求错误: ' + error.message);
    }
    return Promise.reject(error);
  }
);

export default http; 