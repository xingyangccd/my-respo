import http from '../utils/http';

/**
 * Authentication Service
 */
const authService = {
  /**
   * User Login
   * @param {Object} data - Login parameters
   * @param {string} data.username - Username
   * @param {string} data.password - Password
   * @returns {Promise<Object>} Login result with token and user info
   */
  login: async (data) => {
    console.log('authService.login called with:', { username: data.username, password: '******' });
    try {
      // Use native axios for debugging, bypassing http interceptor
      console.log('Sending direct axios request to /api/auth/login');
      const directResponse = await http.post('/auth/login', data, {
        transformResponse: [(data) => {
          // Don't automatically parse JSON to see raw response
          console.log('Raw response data:', data);
          try {
            return JSON.parse(data);
          } catch (e) {
            return data;
          }
        }]
      });
      console.log('Direct response received:', directResponse);

      const { data: responseData } = directResponse;
      console.log('Response data structure:', responseData);
      
      // Check if token exists
      if (responseData && responseData.data && responseData.data.token) {
        const token = responseData.data.token;
        console.log('Token received, saving to localStorage');
        localStorage.setItem('token', token);
      } else if (responseData && responseData.token) {
        console.log('Token directly in response, saving to localStorage');
        localStorage.setItem('token', responseData.token);
      } else {
        console.warn('No token found in response:', responseData);
      }
      
      return responseData.data || responseData;
    } catch (error) {
      console.error('Login request failed:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error request config:', error.config);
      throw error;
    }
  },

  /**
   * User Registration
   * @param {Object} data - Registration parameters
   * @param {string} data.username - Username
   * @param {string} data.password - Password
   * @param {string} data.email - Email
   * @returns {Promise<Object>} Registration result with user info
   */
  register: async (data) => {
    try {
      const response = await http.post('/auth/register', data);
      console.log('Registration response:', response);
      return response;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  },

  /**
   * Get current user information
   * @returns {Promise<Object>} User information
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
   * Logout the current user
   */
  logout: () => {
    localStorage.removeItem('token');
    // Redirect to login page
    window.location.href = '/login';
  },

  /**
   * Check if user is authenticated
   * @returns {boolean} Whether user is authenticated
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  }
};

export default authService; 