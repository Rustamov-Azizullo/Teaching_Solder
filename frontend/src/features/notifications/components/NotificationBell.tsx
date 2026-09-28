import { BellOutlined } from '@ant-design/icons';
import { Badge, Button, Dropdown, Empty, List, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { formatDateTime } from '@/utils/format';
import { useMarkRead, useNotifications, useUnreadCount } from '../hooks/useNotifications';
import { notificationLabels } from '../labels';
import type { AppNotification } from '../types';

const PREVIEW_COUNT = 6;
const PANEL_WIDTH = 340;

export function NotificationBell() {
  const navigate = useNavigate();
  const { data: count = 0 } = useUnreadCount();
  const { data: items = [] } = useNotifications();
  const markRead = useMarkRead();

  const open = (item: AppNotification) => {
    if (!item.read) markRead.mutate(item.id);
    if (item.link) navigate(item.link);
  };

  const panel = (
    <div style={{ width: PANEL_WIDTH, background: 'var(--ant-color-bg-elevated, #fff)', borderRadius: 8, boxShadow: '0 6px 16px rgba(0,0,0,.15)', padding: 8 }}>
      {items.length === 0 ? (
        <Empty description={notificationLabels.empty} image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : (
        <List size="small" dataSource={items.slice(0, PREVIEW_COUNT)}
          renderItem={(item) => (
            <List.Item onClick={() => open(item)} style={{ cursor: 'pointer', opacity: item.read ? 0.6 : 1 }}>
              <List.Item.Meta title={<Typography.Text strong={!item.read}>{item.title}</Typography.Text>} description={<>{item.body}<br /><small>{formatDateTime(item.createdAt)}</small></>} />
            </List.Item>
          )} />
      )}
      <Button type="link" block onClick={() => navigate('/notifications')}>{notificationLabels.showAll}</Button>
    </div>
  );

  return (
    <Dropdown popupRender={() => panel} trigger={['click']} placement="bottomRight">
      <Badge count={count} size="small" offset={[-2, 2]}>
        <Button icon={<BellOutlined />} aria-label={notificationLabels.title} />
      </Badge>
    </Dropdown>
  );
}
