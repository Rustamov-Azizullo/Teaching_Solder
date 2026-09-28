import { MoonOutlined, SunOutlined } from '@ant-design/icons';
import { Switch } from 'antd';
import { common } from '@/lib/i18n';
import { useThemeMode } from './useThemeMode';

export function ThemeModeSwitch() {
  const { mode, setMode } = useThemeMode();
  return (
    <Switch
      checked={mode === 'dark'}
      onChange={(isDark) => setMode(isDark ? 'dark' : 'light')}
      checkedChildren={<MoonOutlined />}
      unCheckedChildren={<SunOutlined />}
      aria-label={common.theme.toggle}
    />
  );
}
