import { useNavigate } from 'react-router-dom';
import type { AppNotification } from '../types';
import { useMarkRead } from './useNotifications';

/** Bildirishnomani ochadi: o'qilgan deb belgilaydi va havolasi bo'lsa shu sahifaga o'tadi. */
export function useOpenNotification(afterOpen?: () => void): (item: AppNotification) => void {
  const navigate = useNavigate();
  const markRead = useMarkRead();
  return (item) => {
    if (!item.read) markRead.mutate(item.id);
    if (item.link) navigate(item.link);
    afterOpen?.();
  };
}
