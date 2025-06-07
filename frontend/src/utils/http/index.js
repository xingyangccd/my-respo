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
    // Extract business data from Result<T> wrapper
    const { code, message: msg, data } = response.data;
    
    if (code === 200 || code === 0) {
      return data;
    } else {
      message.error(msg || 'Request failed');
      return Promise.reject(new Error(msg || 'Request failed'));
    }
  },
  (error) => {
    if (error.response) {
      const { status } = error.response;
      
      switch (status) {
        case 401:
          message.error('Login expired, please log in again');
          localStorage.removeItem('token');
          // Redirect to login page
          window.location.href = '/login';
          break;
        case 403:
          message.error('No permission to access this resource');
          break;
        case 500:
          message.error('Server error, please try again later');
          break;
        default:
          message.error(error.response.data.message || 'Unknown error');
      }
    } else if (error.request) {
      message.error('Network error, please check your connection');
    } else {
      message.error('Request error: ' + error.message);
    }
    return Promise.reject(error);
  }
);

export default http; 