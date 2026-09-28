import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, Button, Form, Input } from 'antd';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { authLabels } from '../labels';
import { useAuth } from '../hooks/useAuth';

type LoginValues = { username: string; password: string };

export function LoginForm() {
  const { login } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setSubmitting] = useState(false);

  const handleFinish = async ({ username, password }: LoginValues) => {
    setSubmitting(true);
    setErrorMessage(null);
    try {
      await login(username, password);
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Form<LoginValues> layout="vertical" onFinish={handleFinish} requiredMark={false}>
      {errorMessage && <Alert type="error" showIcon message={errorMessage} style={{ marginBottom: 16 }} />}
      <Form.Item name="username" label={authLabels.username} rules={[{ required: true, message: authLabels.usernameRequired }]}>
        <Input prefix={<UserOutlined />} autoComplete="username" size="large" autoFocus />
      </Form.Item>
      <Form.Item name="password" label={authLabels.password} rules={[{ required: true, message: authLabels.passwordRequired }]}>
        <Input.Password prefix={<LockOutlined />} autoComplete="current-password" size="large" />
      </Form.Item>
      <Button type="primary" htmlType="submit" size="large" block loading={isSubmitting}>
        {authLabels.submit}
      </Button>
    </Form>
  );
}
