import { ConfigProvider, theme, type ThemeConfig } from 'antd';
import uzUZ from 'antd/locale/uz_UZ';
import type { PropsWithChildren } from 'react';
import { useThemeMode } from './useThemeMode';

const BORDER_RADIUS = 6;

/** Kun rejimi: tiniq oq kartalar, sovuq och fon va "SaaS" ko'k asosiy rang. */
const LIGHT_TOKENS: NonNullable<ThemeConfig['token']> = {
  colorPrimary: '#2563eb',
  colorBgLayout: '#f5f7fb',
  colorBgContainer: '#ffffff',
  colorSuccess: '#16a34a',
  colorWarning: '#d97706',
  colorError: '#dc2626',
  colorBorderSecondary: '#e6eaf2',
};

/** Tun rejimi: sovuq grafit-ko'k fon va yorqin ko'k asosiy rang. */
const DARK_TOKENS: NonNullable<ThemeConfig['token']> = {
  colorPrimary: '#5b8def',
  colorBgLayout: '#0e1116',
  colorBgContainer: '#151a22',
  colorBgElevated: '#1b212b',
  colorText: '#e6e9ef',
  colorTextSecondary: '#9aa4b2',
  colorBorder: '#2a3240',
  colorBorderSecondary: '#222a36',
  colorSuccess: '#34d399',
  colorWarning: '#f59e0b',
  colorError: '#f87171',
};

export function ThemedConfigProvider({ children }: PropsWithChildren) {
  const { mode } = useThemeMode();
  const isDark = mode === 'dark';
  return (
    <ConfigProvider
      locale={uzUZ}
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: { ...(isDark ? DARK_TOKENS : LIGHT_TOKENS), borderRadius: BORDER_RADIUS },
      }}
    >
      {children}
    </ConfigProvider>
  );
}
