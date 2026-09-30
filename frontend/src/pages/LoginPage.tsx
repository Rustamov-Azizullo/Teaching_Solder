import { SafetyCertificateOutlined } from '@ant-design/icons';
import { Typography, theme } from 'antd';
import { Navigate } from 'react-router-dom';
import { ThemeModeSwitch } from '@/app/theme/ThemeModeSwitch';
import { authLabels, LoginBrand, LoginForm, useAuth } from '@/features/auth';
import { common } from '@/lib/i18n';

/** Demo login/parol yozuvi faqat `VITE_SHOW_DEMO_HINT=true` bilan ishga tushirilganda ko'rsatiladi. */
const SHOW_DEMO_HINT = import.meta.env.VITE_SHOW_DEMO_HINT === 'true';

export function LoginPage() {
  const { status } = useAuth();
  const { token } = theme.useToken();

  // Yangi kirishdan keyin har doim bosh sahifa (rolga mos birinchi sahifa): oldingi foydalanuvchi sahifasi boshqa rolda mavjud bo'lmasligi mumkin.
  if (status === 'authenticated') return <Navigate to="/" replace />;

  return (
    <div className="login-page" style={{ background: token.colorBgLayout }}>
      <LoginBrand />
      <main className="login-main">
        <div className="login-main__theme"><ThemeModeSwitch /></div>
        <div
          className="login-card"
          style={{
            background: token.colorBgContainer,
            border: `1px solid ${token.colorBorderSecondary}`,
            borderRadius: token.borderRadiusLG * 2,
            boxShadow: token.boxShadowSecondary,
          }}
        >
          <div className="login-card__mark" style={{ background: token.colorPrimaryBg, color: token.colorPrimary }}>
            <SafetyCertificateOutlined />
          </div>
          <Typography.Title level={2} style={{ marginTop: 0, marginBottom: 4 }}>{authLabels.welcome}</Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 24 }}>
            {common.appName} — {authLabels.loginSubtitle}
          </Typography.Paragraph>
          <LoginForm />
          {SHOW_DEMO_HINT && (
            <Typography.Paragraph type="secondary" style={{ marginTop: 20, marginBottom: 0, fontSize: 12 }}>
              {authLabels.demoHint}
            </Typography.Paragraph>
          )}
        </div>
      </main>
    </div>
  );
}
