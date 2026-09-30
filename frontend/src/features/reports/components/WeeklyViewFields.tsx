import { Form, Radio, Select } from 'antd';
import type { Role } from '@/features/auth';
import { SubdivisionTreeSelect, useMilitaryDistricts, useMilitaryUnits } from '@/features/organization';
import { weeklyLabels, weeklyTargetLabels, weeklyViewLabels } from '../labels';
import type { ReportParams, WeeklyMode, WeeklySelection } from '../types';

type TargetMode = keyof typeof weeklyTargetLabels;

type WeeklyViewFieldsProps = {
  role: Role;
  unitId: number | undefined;
  value: WeeklySelection;
  onChange: (value: WeeklySelection) => void;
};

const isTargetMode = (mode: WeeklyMode): mode is TargetMode => mode in weeklyTargetLabels;

/** Tanlangan ob'ektning so'rov parametri nomi: okrug, harbiy qism yoki bo'linma. */
const targetParamByMode: Record<TargetMode, 'districtId' | 'unitId' | 'subdivisionId'> = {
  DISTRICT: 'districtId', UNIT: 'unitId', SUBDIVISION: 'subdivisionId',
};

/** Haftalik hisobot tanlovini so'rov parametrlariga aylantiradi. */
export function toWeeklyParams({ mode, targetId }: WeeklySelection): Pick<ReportParams, 'view' | 'districtId' | 'unitId' | 'subdivisionId'> {
  return isTargetMode(mode) ? { view: 'TOTAL', [targetParamByMode[mode]]: targetId } : { view: mode };
}

/** Tanlanadigan rejimda ob'ekt ham tanlangan bo'lishi kerak. */
export const isWeeklySelectionComplete = ({ mode, targetId }: WeeklySelection): boolean =>
  !isTargetMode(mode) || targetId !== undefined;

/** Rol uchun boshlang'ich tanlov: User — umumiy, boshqalar — o'z kesimi. */
export const defaultWeeklySelection = (role: Role): WeeklySelection => ({ mode: role === 'USER' ? 'TOTAL' : 'BREAKDOWN' });

function DistrictSelect({ value, onChange }: { value?: number; onChange: (id: number) => void }) {
  const { data = [], isLoading } = useMilitaryDistricts();
  return (
    <Select showSearch optionFilterProp="label" loading={isLoading} value={value} onChange={onChange}
      placeholder={weeklyTargetLabels.DISTRICT.placeholder} options={data.map((d) => ({ value: d.id, label: d.name }))} />
  );
}

function UnitSelect({ value, onChange }: { value?: number; onChange: (id: number) => void }) {
  const { data = [], isLoading } = useMilitaryUnits();
  return (
    <Select showSearch optionFilterProp="label" loading={isLoading} value={value} onChange={onChange}
      placeholder={weeklyTargetLabels.UNIT.placeholder} options={data.map((u) => ({ value: u.id, label: u.name }))} />
  );
}

/** Haftalik hisobot ko'rinishi: rolga mos kesim yoki umumiy, yoxud o'z doirasidagi okrug / harbiy qism / bo'linmani tanlash. */
export function WeeklyViewFields({ role, unitId, value, onChange }: WeeklyViewFieldsProps) {
  const modes = Object.entries(weeklyViewLabels[role]) as [WeeklyMode, string][];
  const select = (targetId: number | undefined) => onChange({ mode: value.mode, targetId });
  return (
    <>
      <Form.Item label={weeklyLabels.view}>
        <Radio.Group value={value.mode} onChange={(event) => onChange({ mode: event.target.value as WeeklyMode })}>
          {modes.map(([mode, label]) => <Radio key={mode} value={mode}>{label}</Radio>)}
        </Radio.Group>
      </Form.Item>
      {isTargetMode(value.mode) && (
        <Form.Item label={weeklyTargetLabels[value.mode].field} required>
          {value.mode === 'DISTRICT' && <DistrictSelect value={value.targetId} onChange={select} />}
          {value.mode === 'UNIT' && <UnitSelect value={value.targetId} onChange={select} />}
          {value.mode === 'SUBDIVISION' && (
            <SubdivisionTreeSelect unitId={unitId} value={value.targetId} allowClear={false}
              placeholder={weeklyTargetLabels.SUBDIVISION.placeholder} style={{ width: '100%' }} onChange={select} />
          )}
        </Form.Item>
      )}
    </>
  );
}
