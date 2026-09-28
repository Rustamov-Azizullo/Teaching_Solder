import { useContext } from 'react';
import { ThemeModeContext, type ThemeModeContextValue } from './ThemeModeProvider';

export function useThemeMode(): ThemeModeContextValue {
  const context = useContext(ThemeModeContext);
  if (!context) throw new Error('useThemeMode faqat <ThemeModeProvider> ichida ishlaydi');
  return context;
}
