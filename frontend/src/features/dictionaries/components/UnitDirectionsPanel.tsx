import { Alert, Button, Checkbox, Space } from 'antd';
import { useEffect, useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { MilitaryUnitSelect } from '@/features/organization';
import { dictionaryLabels } from '../labels';
import { useDictionary, useReplaceUnitDirections, useUnitDirections } from '../hooks/useDictionary';
import { notify } from '@/lib/notify';

export function UnitDirectionsPanel() {
  const [unitId, setUnitId] = useState<number | undefined>();
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const { data: directions = [] } = useDictionary('PROFESSION_DIRECTION');
  const { data: activatedIds } = useUnitDirections(unitId);
  const { mutateAsync, isPending } = useReplaceUnitDirections(unitId);

  useEffect(() => setSelectedIds(activatedIds ?? []), [activatedIds]);

  const handleSave = async () => {
    try {
      await mutateAsync(selectedIds);
      notify.success(dictionaryLabels.unitDirectionsSaved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Alert type="info" showIcon message={dictionaryLabels.unitDirectionsHint} />
      <MilitaryUnitSelect value={unitId} onChange={setUnitId} />
      {unitId !== undefined && (
        <>
          <Checkbox.Group
            value={selectedIds}
            onChange={(values) => setSelectedIds(values as number[])}
            style={{ display: 'grid', gap: 8 }}
            options={directions.map((direction) => ({ value: direction.id, label: direction.name }))}
          />
          <Button type="primary" onClick={handleSave} loading={isPending}>{common.actions.save}</Button>
        </>
      )}
    </Space>
  );
}
