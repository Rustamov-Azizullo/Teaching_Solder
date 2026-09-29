import { Select, Space } from 'antd';
import { useState } from 'react';
import { common } from '@/lib/i18n';
import { useLocationTree } from '../hooks/useOrganization';
import {
  MINISTRY_OPTION_VALUE, districtLocationOfUnit, militaryDistrictIdOf, toDistrictOptions, toUnitOptions,
} from '../utils/districtUnitOptions';

const SELECT_WIDTH = 260;

/** Tanlov: `districtId` — harbiy okrug (vazirlik tanlansa `undefined`), `unitId` — harbiy qism. */
export type DistrictUnitSelection = { districtId?: number; unitId?: number };

type DistrictUnitSelectProps = {
  value: DistrictUnitSelection;
  onChange: (selection: DistrictUnitSelection) => void;
  allowClearUnit?: boolean;
};

/** Harbiy qism tanlash: avval vazirlik/okrug bo'yicha saralanadi, so'ng shu okrugdagi qismlardan tanlanadi. */
export function DistrictUnitSelect({ value, onChange, allowClearUnit }: DistrictUnitSelectProps) {
  const { data: locations = [], isLoading } = useLocationTree();
  const [pickedDistrict, setPickedDistrict] = useState<number | undefined>();
  // Foydalanuvchi okrug tanlamaguncha, qism okrugi qismning o'zidan aniqlanadi.
  const district = pickedDistrict ?? districtLocationOfUnit(locations, value.unitId) ?? MINISTRY_OPTION_VALUE;
  const districtId = militaryDistrictIdOf(locations, district);

  return (
    <Space wrap>
      <Select showSearch optionFilterProp="label" loading={isLoading} value={district} style={{ width: SELECT_WIDTH }}
        options={toDistrictOptions(locations)}
        onChange={(next) => {
          setPickedDistrict(next);
          onChange({ districtId: militaryDistrictIdOf(locations, next), unitId: undefined });
        }} />
      <Select showSearch allowClear={allowClearUnit} optionFilterProp="label" loading={isLoading} value={value.unitId}
        placeholder={common.fields.unit} style={{ width: SELECT_WIDTH }} options={toUnitOptions(locations, district)}
        onChange={(unitId: number | undefined) => onChange({ districtId, unitId })} />
    </Space>
  );
}
