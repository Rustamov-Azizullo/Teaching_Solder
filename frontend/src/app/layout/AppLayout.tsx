import { LogoutOutlined, MenuOutlined } from '@ant-design/icons';
import { Button, Drawer, Layout, Menu, Space, Typography, theme } from 'antd';
import { useMemo, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { can, canAny, canOpenScopedCatalog, isPermissionManager, useAuth, type AuthUser, type Capability } from '@/features/auth';
import { NotificationBell } from '@/features/notifications';
import { useMediaQuery } from '@/hooks/useMediaQuery';
import { common } from '@/lib/i18n';
import { navItems, type NavItem } from '../navigation';
import { ThemeModeSwitch } from '../theme/ThemeModeSwitch';

const { Header, Sider, Content } = Layout;
const SIDER_WIDTH = 240;
const MOBILE_QUERY = '(max-width: 991px)';
const HEADER_HEIGHT = 'var(--app-header-height)';
const HEADER_Z_INDEX = 100;

/** Dashboard menyusi: uch blokdan kamida bittasiga ruxsat bo'lsa ko'rsatiladi. */
const DASHBOARD_CAPABILITIES = ['dashboardVocational', 'dashboardOtm', 'dashboardSurveys'] as const;

function isNavItemVisible(item: NavItem, user: AuthUser | null): boolean {
  if (item.path === '/dashboard') return DASHBOARD_CAPABILITIES.some((capability) => can(user, capability));
  if (item.access === 'unitScoped') return canOpenScopedCatalog(user);
  if (item.access === 'any') return true;
  if (item.access === 'permissionManager') return isPermissionManager(user);
  return Array.isArray(item.access) ? canAny(user, item.access) : can(user, item.access as Capability);
}

export function AppLayout() {
  const { user, logout } = useAuth();
  const { token } = theme.useToken();
  const border = `1px solid ${token.colorBorderSecondary}`;
  const location = useLocation();
  const navigate = useNavigate();
  const isMobile = useMediaQuery(MOBILE_QUERY);
  const [isDrawerOpen, setDrawerOpen] = useState(false);

  const visibleItems = useMemo(() => navItems.filter((item) => isNavItemVisible(item, user)), [user]);
  const selectedKey = visibleItems.find((item) => location.pathname.startsWith(item.path))?.path;

  const goHome = () => {
    setDrawerOpen(false);
    navigate('/');
  };

  /** Ilova nomi bosilganda bosh sahifaga (ruxsatga mos birinchi ish sahifasi, odatda dashboard) qaytadi. */
  const brand = (
    <a onClick={goHome} role="link" aria-label={common.appName} style={{ color: 'inherit', cursor: 'pointer' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>{common.appName}</Typography.Title>
    </a>
  );

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
        <Drawer open={isDrawerOpen} onClose={() => setDrawerOpen(false)} placement="left" width={SIDER_WIDTH} title={brand} styles={{ body: { padding: 0 } }}>
          {menu}
        </Drawer>
      ) : (
        <Sider
          width={SIDER_WIDTH}
          theme="light"
          className="no-print"
          style={{ borderRight: border, position: 'sticky', top: 0, height: '100vh', overflowY: 'auto', flex: `0 0 ${SIDER_WIDTH}px` }}
        >
          <div style={{ padding: 16 }}>{brand}</div>
          {menu}
        </Sider>
      )}
      <Layout>
        <Header
          className="no-print"
          style={{
            position: 'sticky',
            top: 0,
            zIndex: HEADER_Z_INDEX,
            background: token.colorBgContainer,
            display: 'flex',
            justifyContent: isMobile ? 'space-between' : 'flex-end',
            alignItems: 'center',
            padding: '0 16px',
            borderBottom: border,
            height: HEADER_HEIGHT,
            lineHeight: 'normal',
          }}
        >
          {isMobile && <Button icon={<MenuOutlined />} onClick={() => setDrawerOpen(true)} aria-label="Menyu" />}
          <Space>
            <ThemeModeSwitch />
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
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
