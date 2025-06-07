import React, { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import authService from '../api/authService';

const Register = () => {
  const [loading, setLoading] = useState(false);
  
  const onFinish = async (values) => {
    try {
      setLoading(true);
      await authService.register(values);
      message.success('注册成功，请登录');
      
      // 注册成功后，重定向到登录页面
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
    } catch (error) {
      console.error('Registration error:', error);
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
        title="HD Chat 注册"
        style={{ width: 400, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
      >
        <Form
          name="register"
          onFinish={onFinish}
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名!' },
              { min: 3, message: '用户名至少3个字符' },
              { max: 20, message: '用户名最多20个字符' }
            ]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="用户名" 
              size="large" 
            />
          </Form.Item>

          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱!' },
              { type: 'email', message: '请输入有效的邮箱地址!' }
            ]}
          >
            <Input 
              prefix={<MailOutlined />} 
              placeholder="邮箱" 
              size="large" 
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码!' },
              { min: 6, message: '密码至少6个字符' }
            ]}
            hasFeedback
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="密码" 
              size="large" 
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            hasFeedback
            rules={[
              { required: true, message: '请确认密码!' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致!'));
                },
              }),
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="确认密码" 
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
              注册
            </Button>
          </Form.Item>
          
          <div style={{ textAlign: 'center' }}>
            <a href="/login">已有账号? 立即登录</a>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Register; 