import { BellOutlined, ClockCircleOutlined, ExclamationCircleOutlined, FileDoneOutlined, ProfileOutlined, TeamOutlined } from '@ant-design/icons';
import type { ReactNode } from 'react';
import type { AppNotification } from '../types';

export type NotificationTone = 'info' | 'warning' | 'danger';
export type NotificationVisual = { icon: ReactNode; tone: NotificationTone };

const OVERDUE_MARKER = "o'tib";

/** Bildirishnomaning ikonkasi va ohangi: havolasiga qarab bo'lim, "o'tib ketdi" bo'lsa — xavf rangi. */
export function notificationVisual(item: AppNotification): NotificationVisual {
  const link = item.link ?? '';
  if (link.startsWith('/deadlines')) {
    const isOverdue = item.title.toLowerCase().includes(OVERDUE_MARKER);
    return { icon: isOverdue ? <ExclamationCircleOutlined /> : <ClockCircleOutlined />, tone: isOverdue ? 'danger' : 'warning' };
  }
  if (link.startsWith('/employment')) return { icon: <ProfileOutlined />, tone: 'info' };
  if (link.startsWith('/admissions')) return { icon: <FileDoneOutlined />, tone: 'info' };
  if (link.startsWith('/soldiers') || link.startsWith('/groups')) return { icon: <TeamOutlined />, tone: 'info' };
  return { icon: <BellOutlined />, tone: 'info' };
}
