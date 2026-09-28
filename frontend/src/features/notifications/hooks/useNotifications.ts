import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '../api/notificationApi';

const KEY = ['notifications'];
const POLL_INTERVAL_MS = 60_000;

export const useNotifications = () => useQuery({ queryKey: KEY, queryFn: notificationApi.list });

/** Bildirishnomalar soni har daqiqada yangilanadi (tashqi kanal yo'q, faqat tizim ichida). */
export const useUnreadCount = () =>
  useQuery({ queryKey: [...KEY, 'unread'], queryFn: notificationApi.unreadCount, refetchInterval: POLL_INTERVAL_MS });

function useRefreshing<TVariables>(fn: (v: TVariables) => Promise<void>) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: fn, onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }) });
}

export const useMarkRead = () => useRefreshing((id: number) => notificationApi.markRead(id));
export const useMarkAllRead = () => useRefreshing(() => notificationApi.markAllRead());
