import { App } from 'antd';
import { useEffect } from 'react';
import { bindNotify } from '@/lib/notify';

export function NotifyBinder() {
  const { message } = App.useApp();
  useEffect(() => bindNotify(message), [message]);
  return null;
}
