import { Spin } from 'antd';

export function FullPageSpinner() {
  return (
    <div style={{ display: 'grid', placeItems: 'center', minHeight: '60vh' }}>
      <Spin size="large" />
    </div>
  );
}
