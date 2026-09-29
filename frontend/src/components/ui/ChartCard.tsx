import { Card, Empty, Spin } from 'antd';
import type { ReactNode } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { ErrorState } from './ErrorState';

type ChartCardProps = {
  title: string;
  /** Sarlavha oldidagi ixtiyoriy ikonka. */
  icon?: ReactNode;
  isEmpty?: boolean;
  isLoading?: boolean;
  error?: unknown;
  onRetry?: () => void;
  extra?: ReactNode;
  children: ReactNode;
};

const CARD_PADDING = 8;
const HEADER_MIN_HEIGHT = 36;

/** Diagramma kartasi: ajratilgan joyni to'liq egallaydi; ichidagi diagramma `height="100%"` bilan chiziladi. */
export function ChartCard({ title, icon, isEmpty, isLoading, error, onRetry, extra, children }: ChartCardProps) {
  const renderContent = () => {
    if (isLoading) return <div style={{ display: 'grid', placeItems: 'center', height: '100%' }}><Spin /></div>;
    if (error) return <ErrorState message={getErrorMessage(error)} onRetry={onRetry} />;
    if (isEmpty) return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={common.states.empty} />;
    return children;
  };

  return (
    <Card
      title={icon ? <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>{icon}{title}</span> : title}
      extra={extra}
      size="small"
      style={{ height: '100%', display: 'flex', flexDirection: 'column', minHeight: 0 }}
      styles={{
        header: { minHeight: HEADER_MIN_HEIGHT, padding: '0 12px', fontSize: 13 },
        body: { flex: 1, minHeight: 0, padding: CARD_PADDING },
      }}
    >
      <div style={{ height: '100%', minHeight: 0 }}>{renderContent()}</div>
    </Card>
  );
}
