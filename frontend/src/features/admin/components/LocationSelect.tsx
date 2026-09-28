import { Select } from 'antd';
import { useMemo } from 'react';
import type { LocationLevel } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { useLocations } from '../hooks/useAdmin';
import { buildLocationOptions } from '../utils/locationOptions';

type LocationSelectProps = {
  level: LocationLevel;
  /** `Form.Item` tomonidan uzatiladi. */
  value?: number;
  onChange?: (value: number) => void;
};

export function LocationSelect({ level, value, onChange }: LocationSelectProps) {
  const { data: locations = [], isLoading, error } = useLocations();
  const options = useMemo(() => buildLocationOptions(locations, level), [locations, level]);
  return (
    <Select
      value={value}
      onChange={onChange}
      options={options}
      loading={isLoading}
      status={error ? 'error' : undefined}
      notFoundContent={error ? getErrorMessage(error) : undefined}
      showSearch
      optionFilterProp="label"
    />
  );
}
