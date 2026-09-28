import { QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp, ConfigProvider } from 'antd';
import uzUZ from 'antd/locale/uz_UZ';
import { RouterProvider } from 'react-router-dom';
import { AuthProvider } from '@/features/auth';
import { queryClient } from '@/lib/queryClient';
import { NotifyBinder } from './NotifyBinder';
import { router } from './router';

export function App() {
  return (
    <ConfigProvider locale={uzUZ} theme={{ token: { colorPrimary: '#1f5f3f', borderRadius: 6 } }}>
      <AntApp>
        <NotifyBinder />
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <RouterProvider router={router} />
          </AuthProvider>
        </QueryClientProvider>
      </AntApp>
    </ConfigProvider>
  );
}
