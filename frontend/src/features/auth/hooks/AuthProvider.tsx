import { createContext, useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import { AUTH_EXPIRED_EVENT } from '@/lib/apiClient';
import { queryClient } from '@/lib/queryClient';
import { tokenStorage } from '@/lib/tokenStorage';
import { authApi } from '../api/authApi';
import type { AuthStatus, AuthUser } from '../types';

export type AuthContextValue = {
  user: AuthUser | null;
  status: AuthStatus;
  login: (username: string, password: string) => Promise<void>;
  refreshUser: () => Promise<void>;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthUser | null>(null);
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

  const login = useCallback(async (username: string, password: string) => {
    const response = await authApi.login(username, password);
    tokenStorage.set(response.accessToken);
    setUser(response.user);
    setStatus('authenticated');
  }, []);

  const refreshUser = useCallback(async () => {
    const me = await authApi.me();
    setUser(me);
  }, []);

  const value = useMemo(
    () => ({ user, status, login, refreshUser, logout }),
    [user, status, login, refreshUser, logout],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
