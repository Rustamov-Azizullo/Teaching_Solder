import axios, { AxiosError } from 'axios';
import { tokenStorage } from '@/lib/tokenStorage';
import type { ApiErrorBody } from '@/types/api';

const UNAUTHORIZED = 401;
export const AUTH_EXPIRED_EVENT = 'askar:auth-expired';

export const apiClient = axios.create({ baseURL: '/api', timeout: 20_000 });

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => {
    const isLoginRequest = error.config?.url?.includes('/auth/login');
    if (error.response?.status === UNAUTHORIZED && !isLoginRequest) {
      tokenStorage.clear();
      window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    }
    return Promise.reject(error);
  },
);

const FALLBACK_MESSAGE = "Kutilmagan xatolik yuz berdi. Qayta urinib ko'ring";
const NETWORK_MESSAGE = "Serverga ulanib bo'lmadi. Internet aloqasini tekshiring";

/** Server xabarini foydalanuvchiga tushunarli matnga aylantiradi. */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    if (!error.response) return NETWORK_MESSAGE;
    return error.response.data?.message ?? FALLBACK_MESSAGE;
  }
  return FALLBACK_MESSAGE;
}

export function getFieldErrors(error: unknown): Record<string, string> {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    return error.response?.data?.fieldErrors ?? {};
  }
  return {};
}
