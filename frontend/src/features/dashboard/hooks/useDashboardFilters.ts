import { useState } from 'react';
import { API_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import type { BreakdownLevel, BreakdownRow, DashboardFilters } from '../types';

const DEFAULT_PERIOD_DAYS = 30;

type DrillLabels = { district?: string; unit?: string };

/** Davr va drill-down holati: okrug → qism → guruh. */
export function useDashboardFilters() {
  const [filters, setFilters] = useState<DashboardFilters>(() => ({
    from: dayjs().subtract(DEFAULT_PERIOD_DAYS, 'day').format(API_DATE_FORMAT),
    to: dayjs().format(API_DATE_FORMAT),
  }));
  const [drillLabels, setDrillLabels] = useState<DrillLabels>({});

  const setPeriod = (from: string, to: string) => setFilters((current) => ({ ...current, from, to }));

  const drillInto = (level: BreakdownLevel, row: BreakdownRow) => {
    if (level === 'DISTRICT') {
      setFilters((current) => ({ ...current, districtId: row.id, unitId: undefined }));
      setDrillLabels({ district: row.label });
    } else if (level === 'UNIT') {
      setFilters((current) => ({ ...current, unitId: row.id }));
      setDrillLabels((current) => ({ ...current, unit: row.label }));
    }
  };

  const reset = () => {
    setFilters(({ from, to }) => ({ from, to }));
    setDrillLabels({});
  };

  return { filters, drillLabels, setPeriod, drillInto, reset };
}
