import { Typography, theme } from 'antd';
import { formatRelativeTime } from '@/utils/format';
import type { AppNotification } from '../types';
import { notificationVisual, type NotificationTone } from './notificationVisual';

const ICON_SIZE = 34;

type NotificationItemProps = { item: AppNotification; onOpen: (item: AppNotification) => void };

/** Bitta bildirishnoma qatori (qo'ng'iroqcha paneli va to'liq ro'yxat uchun umumiy). */
export function NotificationItem({ item, onOpen }: NotificationItemProps) {
  const { token } = theme.useToken();
  const visual = notificationVisual(item);
  const toneColors: Record<NotificationTone, { color: string; background: string }> = {
    info: { color: token.colorPrimary, background: token.colorPrimaryBg },
    warning: { color: token.colorWarning, background: token.colorWarningBg },
    danger: { color: token.colorError, background: token.colorErrorBg },
  };
  const tone = toneColors[visual.tone];

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => onOpen(item)}
      onKeyDown={(event) => { if (event.key === 'Enter') onOpen(item); }}
      className="notification-item"
      style={{ display: 'flex', gap: 12, padding: '10px 12px', cursor: 'pointer', borderRadius: token.borderRadius, opacity: item.read ? 0.7 : 1 }}
    >
      <div style={{ ...tone, width: ICON_SIZE, height: ICON_SIZE, flex: `0 0 ${ICON_SIZE}px`, borderRadius: '50%', display: 'grid', placeItems: 'center' }}>
        {visual.icon}
      </div>
      <div style={{ minWidth: 0, flex: 1 }}>
        <Typography.Text strong={!item.read} style={{ display: 'block' }} ellipsis>{item.title}</Typography.Text>
        <Typography.Paragraph type="secondary" style={{ margin: 0, fontSize: 12 }} ellipsis={{ rows: 2 }}>{item.body}</Typography.Paragraph>
        <Typography.Text type="secondary" style={{ fontSize: 11 }}>{formatRelativeTime(item.createdAt)}</Typography.Text>
      </div>
      {!item.read && <span aria-label="O'qilmagan" style={{ width: 8, height: 8, borderRadius: '50%', background: token.colorPrimary, marginTop: 6, flex: '0 0 8px' }} />}
    </div>
  );
}
