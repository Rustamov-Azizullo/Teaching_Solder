import { apiClient } from '@/lib/apiClient';
import type { AuthUser, LoginResponse } from '../types';

export const authApi = {
  login: (username: string, password: string) =>
    apiClient.post<LoginResponse>('/auth/login', { username, password }).then((r) => r.data),
  me: () => apiClient.get<AuthUser>('/auth/me').then((r) => r.data),
};
