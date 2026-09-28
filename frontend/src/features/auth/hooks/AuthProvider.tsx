import { createContext, useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import { AUTH_EXPIRED_EVENT } from '@/lib/apiClient';
import { queryClient } from '@/lib/queryClient';
import { tokenStorage } from '@/lib/tokenStorage';
import { authApi } from '../api/authApi';
import type { AuthStatus, AuthUser, Role } from '../types';

export type AuthContextValue = {
  user: AuthUser | null;
  status: AuthStatus;
  setupRequired: boolean;
  login: (username: string, password: string, otp?: string) => Promise<void>;
  refreshUser: () => Promise<void>;
  logout: () => void;
};

/** Okrug darajasi va undan yuqori rollar (qism darajasidagi `USER` bundan mustasno). */
const REMINDED_ROLES: Role[] = ['MEGA_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN'];

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [setupRequired, setSetupRequired] = useState(false);
  const [status, setStatus] = useState<AuthStatus>(tokenStorage.get() ? 'loading' : 'anonymous');

  const logout = useCallback(() => {
    tokenStorage.clear();
    queryClient.clear();
    setUser(null);
    setStatus('anonymous');
  }, []);

  useEffect(() => {
    if (!tokenStorage.get()) return;
    authApi
      .me()
      .then((me) => {
        setUser(me);
        setStatus('authenticated');
      })
      .catch(logout);
  }, [logout]);

  useEffect(() => {
    window.addEventListener(AUTH_EXPIRED_EVENT, logout);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, logout);
  }, [logout]);

  const login = useCallback(async (username: string, password: string, otp?: string) => {
    const response = await authApi.login(username, password, otp);
    tokenStorage.set(response.accessToken);
    setUser(response.user);
    setSetupRequired(response.twoFactorSetupRequired);
    setStatus('authenticated');
  }, []);

  const refreshUser = useCallback(async () => {
    const me = await authApi.me();
    setUser(me);
    if (me.twoFactorEnabled) setSetupRequired(false);
  }, []);

  // Respublika va okrug rollari uchun 2FA majburiy: kirish bloklanmaydi, faqat eslatma ko'rsatiladi.
  const mustSetUp = user !== null && REMINDED_ROLES.includes(user.role) && !user.twoFactorEnabled;
  const value = useMemo(
    () => ({ user, status, setupRequired: setupRequired || mustSetUp, login, refreshUser, logout }),
    [user, status, setupRequired, mustSetUp, login, refreshUser, logout],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
