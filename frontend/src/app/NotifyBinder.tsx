import { App } from 'antd';
import { useEffect } from 'react';
import { bindNotify } from '@/lib/notify';

/** <AntApp> ichida joylashib, kontekstli `message` ni butun ilova uchun ulaydi. */
export function NotifyBinder() {
  const { message } = App.useApp();
  useEffect(() => bindNotify(message), [message]);
  return null;
}
