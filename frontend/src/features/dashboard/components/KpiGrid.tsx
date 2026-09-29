import { Card, theme } from 'antd';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

/** `to` berilsa, karta bosilganda shu manzilga o'tadi; `icon` raqam yonida ko'rsatiladi. */
export type KpiItem = { title: string; value: number | string | undefined; to?: string; icon?: ReactNode };

const ICON_SIZE = 18;
const ICON_GAP = 8;

/** Yuqori qator: bir xil kenglikdagi ko'rsatkich kartalari (katta raqam, ostida yorliq). */
export function KpiGrid({ items }: { items: KpiItem[] }) {
  const { token } = theme.useToken();
  return (
    <div className="dashboard-kpis">
      {items.map((item) => {
        const card = (
          <Card hoverable={item.to !== undefined} size="small" style={{ height: '100%' }}
            styles={{ body: { padding: '10px 8px', textAlign: 'center' } }}>
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: ICON_GAP,
              fontSize: 'clamp(20px, 3vh, 28px)', fontWeight: 600, lineHeight: 1.15 }}>
              {item.icon && <span style={{ fontSize: ICON_SIZE, color: token.colorPrimary, display: 'inline-flex' }}>{item.icon}</span>}
              {item.value ?? '—'}
            </div>
            <div style={{ fontSize: 12, opacity: 0.65, marginTop: 2 }}>{item.title}</div>
          </Card>
        );
        return item.to ? <Link key={item.title} to={item.to} style={{ color: 'inherit' }}>{card}</Link> : <div key={item.title}>{card}</div>;
      })}
    </div>
  );
}
