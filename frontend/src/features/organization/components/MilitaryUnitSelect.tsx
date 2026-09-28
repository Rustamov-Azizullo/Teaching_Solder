import { Select } from 'antd';
import { common } from '@/lib/i18n';
import { useMilitaryUnits } from '../hooks/useOrganization';

type MilitaryUnitSelectProps = {
  value?: number;
  onChange?: (value: number | undefined) => void;
  allowClear?: boolean;
  style?: React.CSSProperties;
};

export function MilitaryUnitSelect({ value, onChange, allowClear, style }: MilitaryUnitSelectProps) {
  const { data = [], isLoading } = useMilitaryUnits();
  return (
    <Select
      showSearch
      optionFilterProp="label"
      loading={isLoading}
      value={value}
      onChange={onChange}
      allowClear={allowClear}
      placeholder={common.fields.unit}
      style={{ minWidth: 220, ...style }}
      options={data.map((unit) => ({ value: unit.id, label: unit.name }))}
    />
  );
}
