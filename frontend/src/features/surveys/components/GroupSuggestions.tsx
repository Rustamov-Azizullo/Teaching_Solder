import { Collapse, List, Space, Typography } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { MilitaryUnitSelect } from '@/features/organization';
import { useSuggestions } from '../hooks/useSuggestions';
import { surveyLabels } from '../labels';

/** So'rovnoma natijalaridan guruhlarga taqsimot taklifi; yakuniy taqsimotni qism xodimi tasdiqlaydi. */
export function GroupSuggestions() {
  const [unitId, setUnitId] = useState<number | undefined>();
  const { data, isLoading, error, refetch } = useSuggestions(unitId);
  const t = surveyLabels.suggestions;
  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Typography.Text type="secondary">{t.hint}</Typography.Text>
      <MilitaryUnitSelect allowClear value={unitId} onChange={setUnitId} />
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch} isEmpty={(items) => items.length === 0}>
        {(items) => (
          <Collapse
            items={items.map((item, index) => ({
              key: `${item.kind}-${index}`,
              label: `${item.kind === 'DIRECTION' ? t.direction : t.subject}: ${item.label} (${item.soldiers.length})`,
              children: <List size="small" dataSource={item.soldiers} renderItem={(s) => <List.Item>{s.fullName}</List.Item>} />,
            }))}
          />
        )}
      </QueryBoundary>
    </Space>
  );
}
