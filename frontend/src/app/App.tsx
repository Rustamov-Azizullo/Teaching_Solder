import { QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp } from 'antd';
import { RouterProvider } from 'react-router-dom';
import { AuthProvider } from '@/features/auth';
import { queryClient } from '@/lib/queryClient';
import { NotifyBinder } from './NotifyBinder';
import { router } from './router';
import { ThemeModeProvider, ThemedConfigProvider } from './theme';

export function App() {
  return (
    <ThemeModeProvider>
      <ThemedConfigProvider>
      <AntApp>
        <NotifyBinder />
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <RouterProvider router={router} />
          </AuthProvider>
        </QueryClientProvider>
      </AntApp>
      </ThemedConfigProvider>
    </ThemeModeProvider>
  );
}
