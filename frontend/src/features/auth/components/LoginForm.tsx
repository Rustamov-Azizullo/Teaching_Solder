import { LockOutlined, SafetyOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, Button, Form, Input } from 'antd';
import axios from 'axios';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import type { ApiErrorBody } from '@/types/api';
import { authLabels } from '../labels';
import { useAuth } from '../hooks/useAuth';

type LoginValues = { username: string; password: string; otp?: string };

const OTP_REQUIRED = 'OTP_REQUIRED';

export function LoginForm() {
  const { login } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isOtpStep, setOtpStep] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);

  const handleFinish = async ({ username, password, otp }: LoginValues) => {
    setSubmitting(true);
    setErrorMessage(null);
    try {
      await login(username, password, otp);
    } catch (error) {
      const code = axios.isAxiosError<ApiErrorBody>(error) ? error.response?.data?.code : undefined;
      setOtpStep(code === OTP_REQUIRED || isOtpStep);
      setErrorMessage(code === OTP_REQUIRED ? authLabels.otpRequired : getErrorMessage(error));
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
      {isOtpStep && (
        <Form.Item name="otp" label={authLabels.otpLabel} rules={[{ required: true, len: 6, message: authLabels.otpRequired }]}>
          <Input prefix={<SafetyOutlined />} inputMode="numeric" maxLength={6} autoComplete="one-time-code" size="large" autoFocus />
        </Form.Item>
      )}
      <Button type="primary" htmlType="submit" size="large" block loading={isSubmitting}>
        {authLabels.submit}
      </Button>
    </Form>
  );
}
