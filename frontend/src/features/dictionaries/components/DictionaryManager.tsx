import { Tabs } from 'antd';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { dictionaryLabels } from '../labels';
import { useDictionaryTypes } from '../hooks/useDictionary';
import { DictionaryTable } from './DictionaryTable';
import { UnitDirectionsPanel } from './UnitDirectionsPanel';

const UNIT_DIRECTIONS_TAB = 'UNIT_DIRECTIONS';

export function DictionaryManager() {
  const canConfigureUnits = useCan('unitDirections');
  const { data, isLoading, error, refetch } = useDictionaryTypes();

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(types) => (
        <Tabs
          items={[
            ...types.map((type) => ({ key: type.code, label: type.label, children: <DictionaryTable type={type.code} /> })),
            ...(canConfigureUnits
              ? [{ key: UNIT_DIRECTIONS_TAB, label: dictionaryLabels.unitDirectionsTitle, children: <UnitDirectionsPanel /> }]
              : []),
          ]}
        />
      )}
    </QueryBoundary>
  );
}
