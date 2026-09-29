import { EnvironmentOutlined, ReadOutlined, SafetyCertificateOutlined, TeamOutlined } from '@ant-design/icons';
import type { ReactNode } from 'react';
import { common } from '@/lib/i18n';
import { authLabels } from '../labels';

const FEATURE_ICONS: Record<string, ReactNode> = {
  soldiers: <TeamOutlined />,
  courses: <ReadOutlined />,
  monitoring: <EnvironmentOutlined />,
};

/** Kirish sahifasining chap (brend) paneli: nom, qisqa tavsif va tizim imkoniyatlari. */
export function LoginBrand() {
  return (
    <aside className="login-brand" aria-hidden="true">
      <div className="login-brand__content">
        <div className="login-brand__mark"><SafetyCertificateOutlined /></div>
        <h1 className="login-brand__title">{common.appName}</h1>
        <p className="login-brand__subtitle">{common.appSubtitle}</p>
        <ul className="login-brand__features">
          {authLabels.brandFeatures.map((feature) => (
            <li key={feature.key}>
              <span className="login-brand__icon">{FEATURE_ICONS[feature.key]}</span>
              <span>
                <strong>{feature.title}</strong>
                <small>{feature.text}</small>
              </span>
            </li>
          ))}
        </ul>
      </div>
      <p className="login-brand__footer">{authLabels.brandFooter}</p>
    </aside>
  );
}
