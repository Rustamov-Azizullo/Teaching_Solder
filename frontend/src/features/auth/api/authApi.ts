import { apiClient } from '@/lib/apiClient';
import type { AuthUser, LoginResponse, TwoFactorSetup } from '../types';

export const authApi = {
  login: (username: string, password: string, otp?: string) =>
    apiClient.post<LoginResponse>('/auth/login', { username, password, otp }).then((r) => r.data),
  me: () => apiClient.get<AuthUser>('/auth/me').then((r) => r.data),
  setupTwoFactor: () => apiClient.post<TwoFactorSetup>('/auth/2fa/setup').then((r) => r.data),
  enableTwoFactor: (code: string) => apiClient.post('/auth/2fa/enable', { code }).then(() => undefined),
  disableTwoFactor: (code: string) => apiClient.post('/auth/2fa/disable', { code }).then(() => undefined),
};
