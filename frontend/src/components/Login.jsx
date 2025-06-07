import React, { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import authService from '../api/authService';

const Login = () => {
  const [loading, setLoading] = useState(false);
  
  const onFinish = async (values) => {
    console.log('Form values submitted:', values); // Debug log
    try {
      setLoading(true);
      console.log('Calling authService.login...');
      const response = await authService.login(values);
      console.log('Login API response:', response);
      message.success('Login successful');
      
      // Redirect to homepage after successful login
      setTimeout(() => {
        window.location.href = '/chat';
      }, 1000);
    } catch (error) {
      console.error('Login error details:', error);
      // Display specific error message
      message.error(`Login failed: ${error.message || 'Unknown error'}`);
    } finally {
      setLoading(false);
    }
  };

  // Debug function to test direct API call
  const testDirectApiCall = async () => {
    try {
      console.log('Testing direct API call...');
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: 'testuser',
          password: 'testpassword'
        })
      });
      const data = await response.json();
      console.log('Direct API call response:', data);
    } catch (error) {
      console.error('Direct API call error:', error);
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh',
      background: '#f0f2f5'
    }}>
      <Card
        title="HD Chat Login"
        style={{ width: 400, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
      >
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Please enter your username!' }]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="Username" 
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please enter your password!' }]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="Password" 
              size="large" 
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              style={{ width: '100%' }} 
              size="large"
              loading={loading}
            >
              Login
            </Button>
          </Form.Item>
          
          <div style={{ textAlign: 'center' }}>
            <a href="/register">Don't have an account? Register now</a>
          </div>

          {/* Debug button to test API directly */}
          <div style={{ marginTop: '20px' }}>
            <Button 
              type="link" 
              onClick={testDirectApiCall}
              style={{ padding: 0, height: 'auto', fontSize: '12px', color: '#999' }}
            >
              Test API Call
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Login; 