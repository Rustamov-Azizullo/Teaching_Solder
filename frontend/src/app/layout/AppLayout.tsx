import { LogoutOutlined, MenuOutlined } from '@ant-design/icons';
import { Alert, Button, Drawer, Layout, Menu, Space, Typography } from 'antd';
import { useMemo, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { can, useAuth } from '@/features/auth';
import { NotificationBell } from '@/features/notifications';
import { useMediaQuery } from '@/hooks/useMediaQuery';
import { common } from '@/lib/i18n';
import { navItems } from '../navigation';

const { Header, Sider, Content } = Layout;
const SIDER_WIDTH = 230;
const MOBILE_QUERY = '(max-width: 991px)';
const BORDER = '1px solid rgba(128,128,128,.2)';

/** Dashboard menyusi: uch blokdan kamida bittasiga ruxsat bo'lsa ko'rsatiladi. */
const DASHBOARD_CAPABILITIES = ['dashboardVocational', 'dashboardOtm', 'dashboardSurveys'] as const;

export function AppLayout() {
  const { user, logout, setupRequired } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const isMobile = useMediaQuery(MOBILE_QUERY);
  const [isDrawerOpen, setDrawerOpen] = useState(false);

  const visibleItems = useMemo(
    () =>
      navItems.filter((item) => {
        if (item.path === '/dashboard') return DASHBOARD_CAPABILITIES.some((capability) => can(user?.role, capability));
        return item.capability === 'any' || can(user?.role, item.capability);
      }),
    [user],
  );
  const selectedKey = visibleItems.find((item) => location.pathname.startsWith(item.path))?.path;

  const menu = (
    <Menu
      mode="inline"
      selectedKeys={selectedKey ? [selectedKey] : []}
      style={{ borderInlineEnd: 0 }}
      onClick={({ key }) => {
        setDrawerOpen(false);
        navigate(key);
      }}
      items={visibleItems.map((item) => ({ key: item.path, icon: item.icon, label: item.label }))}
    />
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {isMobile ? (
        <Drawer open={isDrawerOpen} onClose={() => setDrawerOpen(false)} placement="left" width={SIDER_WIDTH} title={common.appName} styles={{ body: { padding: 0 } }}>
          {menu}
        </Drawer>
      ) : (
        <Sider width={SIDER_WIDTH} theme="light" style={{ borderRight: BORDER }}>
          <div style={{ padding: 16 }}>
            <Typography.Title level={4} style={{ margin: 0 }}>{common.appName}</Typography.Title>
          </div>
          {menu}
        </Sider>
      )}
      <Layout>
        <Header
          className="no-print"
          style={{ background: 'transparent', display: 'flex', justifyContent: isMobile ? 'space-between' : 'flex-end', alignItems: 'center', padding: '0 16px', borderBottom: BORDER, height: 64, lineHeight: 'normal' }}
        >
          {isMobile && <Button icon={<MenuOutlined />} onClick={() => setDrawerOpen(true)} aria-label="Menyu" />}
          <Space>
            <NotificationBell />
            <a onClick={() => navigate('/profile')} style={{ textAlign: 'right', lineHeight: 1.25, cursor: 'pointer', color: 'inherit' }}>
              <strong>{user?.fullName}</strong>
              <br />
              <small style={{ opacity: 0.7 }}>{user?.roleLabel}</small>
            </a>
            <Button icon={<LogoutOutlined />} onClick={logout} aria-label={common.actions.logout} />
          </Space>
        </Header>
        <Content className="app-content">
          {setupRequired && location.pathname !== '/profile' && (
            <Alert
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
              message="Xavfsizlik uchun ikki bosqichli autentifikatsiyani yoqing"
              action={<Button size="small" onClick={() => navigate('/profile')}>Yoqish</Button>}
            />
          )}
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
