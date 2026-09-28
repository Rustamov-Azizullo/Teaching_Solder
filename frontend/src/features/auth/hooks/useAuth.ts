import { useContext } from 'react';
import { AuthContext, type AuthContextValue } from './AuthProvider';

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth faqat <AuthProvider> ichida ishlaydi');
  return context;
}
