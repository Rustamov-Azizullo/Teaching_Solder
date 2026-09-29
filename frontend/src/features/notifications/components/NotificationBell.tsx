import { BellOutlined, CheckOutlined } from '@ant-design/icons';
import { Badge, Button, Dropdown, Empty, Typography, theme } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMarkAllRead, useNotifications, useUnreadCount } from '../hooks/useNotifications';
import { useOpenNotification } from '../hooks/useOpenNotification';
import { notificationLabels } from '../labels';
import { NotificationItem } from './NotificationItem';

const PREVIEW_COUNT = 8;
const PANEL_WIDTH = 380;
const LIST_MAX_HEIGHT = 420;

export function NotificationBell() {
  const navigate = useNavigate();
  const { token } = theme.useToken();
  const [isOpen, setOpen] = useState(false);
  const { data: count = 0 } = useUnreadCount();
  const { data: items = [] } = useNotifications();
  const markAll = useMarkAllRead();
  const open = useOpenNotification(() => setOpen(false));

  const panel = (
    <div style={{ width: PANEL_WIDTH, maxWidth: '92vw', background: token.colorBgElevated, borderRadius: token.borderRadiusLG, boxShadow: token.boxShadowSecondary }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 16px', borderBottom: `1px solid ${token.colorBorderSecondary}` }}>
        <Typography.Text strong>{notificationLabels.title}{count > 0 && ` (${count})`}</Typography.Text>
        <Button type="link" size="small" icon={<CheckOutlined />} disabled={count === 0} loading={markAll.isPending} onClick={() => markAll.mutate()}>
          {notificationLabels.markAllShort}
        </Button>
      </div>
      <div style={{ maxHeight: LIST_MAX_HEIGHT, overflowY: 'auto', padding: 4 }}>
        {items.length === 0 ? (
          <Empty description={notificationLabels.empty} image={Empty.PRESENTED_IMAGE_SIMPLE} style={{ padding: 24 }} />
        ) : (
          items.slice(0, PREVIEW_COUNT).map((item) => <NotificationItem key={item.id} item={item} onOpen={open} />)
        )}
      </div>
      <div style={{ borderTop: `1px solid ${token.colorBorderSecondary}`, textAlign: 'center' }}>
        <Button type="link" block onClick={() => { setOpen(false); navigate('/notifications'); }}>{notificationLabels.showAll}</Button>
      </div>
    </div>
  );

  return (
    <Dropdown open={isOpen} onOpenChange={setOpen} popupRender={() => panel} trigger={['click']} placement="bottomRight">
      <Badge count={count} size="small" offset={[-2, 2]}>
        <Button icon={<BellOutlined />} aria-label={notificationLabels.title} />
      </Badge>
    </Dropdown>
  );
}
