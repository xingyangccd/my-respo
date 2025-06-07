import http from '../utils/http';

/**
 * 用户认证服务
 */
const authService = {
  /**
   * 用户登录
   * @param {Object} data - 登录参数
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   * @returns {Promise<Object>} 登录结果，包含token和用户信息
   */
  login: async (data) => {
    try {
      const response = await http.post('/auth/login', data);
      // 保存token到本地存储
      if (response && response.token) {
        localStorage.setItem('token', response.token);
      }
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  },

  /**
   * 用户注册
   * @param {Object} data - 注册参数
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   * @param {string} data.email - 邮箱
   * @returns {Promise<Object>} 注册结果，包含用户信息
   */
  register: async (data) => {
    try {
      const response = await http.post('/auth/register', data);
      return response;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  },

  /**
   * 获取当前用户信息
   * @returns {Promise<Object>} 用户信息
   */
  getCurrentUser: async () => {
    try {
      return await http.get('/auth/user');
    } catch (error) {
      console.error('Failed to get current user:', error);
      throw error;
    }
  },

  /**
   * 退出登录
   */
  logout: () => {
    localStorage.removeItem('token');
    // 可以在这里执行重定向到登录页面
    window.location.href = '/login';
  },

  /**
   * 检查用户是否已登录
   * @returns {boolean} 是否已登录
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  }
};

export default authService; 