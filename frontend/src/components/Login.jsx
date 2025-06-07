import React, { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import authService from '../api/authService';

const Login = () => {
  const [loading, setLoading] = useState(false);
  
  const onFinish = async (values) => {
    try {
      setLoading(true);
      const response = await authService.login(values);
      message.success('登录成功');
      
      // 登录成功后，重定向到主页或其他页面
      setTimeout(() => {
        window.location.href = '/chat';
      }, 1000);
      
      console.log('Login successful:', response);
    } catch (error) {
      console.error('Login error:', error);
      // 错误处理在 http 拦截器中已经完成
    } finally {
      setLoading(false);
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
        title="HD Chat 登录"
        style={{ width: 400, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
      >
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名!' }]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="用户名" 
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码!' }]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="密码" 
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
              登录
            </Button>
          </Form.Item>
          
          <div style={{ textAlign: 'center' }}>
            <a href="/register">没有账号? 立即注册</a>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Login; 