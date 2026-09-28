import { apiClient } from '@/lib/apiClient';
import type { AppNotification } from '../types';

export const notificationApi = {
  list: () => apiClient.get<AppNotification[]>('/notifications').then((r) => r.data),
  unreadCount: () => apiClient.get<{ count: number }>('/notifications/unread-count').then((r) => r.data.count),
  markRead: (id: number) => apiClient.post(`/notifications/${id}/read`).then(() => undefined),
  markAllRead: () => apiClient.post('/notifications/read-all').then(() => undefined),
};
