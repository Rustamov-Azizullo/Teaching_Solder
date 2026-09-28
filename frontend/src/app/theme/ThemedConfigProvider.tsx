import { ConfigProvider, theme } from 'antd';
import uzUZ from 'antd/locale/uz_UZ';
import type { PropsWithChildren } from 'react';
import { useThemeMode } from './useThemeMode';

const PRIMARY_COLOR = '#1f5f3f';
const DARK_PRIMARY_COLOR = '#3fa876';
const BORDER_RADIUS = 6;

export function ThemedConfigProvider({ children }: PropsWithChildren) {
  const { mode } = useThemeMode();
  const isDark = mode === 'dark';
  return (
    <ConfigProvider
      locale={uzUZ}
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: { colorPrimary: isDark ? DARK_PRIMARY_COLOR : PRIMARY_COLOR, borderRadius: BORDER_RADIUS },
      }}
    >
      {children}
    </ConfigProvider>
  );
}
