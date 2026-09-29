import { useMemo, useState } from 'react';
import { MINISTRY_OPTION_VALUE, toDistrictOptions, toUnitOptions, type LocationNode } from '@/features/organization';
import type { AdmissionRow } from '../types';

/**
 * Saralash tartibi: vazirlik → okrug → harbiy qism, so'ng F.I.Sh. bo'yicha qidiruv.
 * Okruglar va qism–okrug bog'liqligi hududlar jadvalidan (backend) olinadi.
 */
export function useAdmissionFilters(rows: AdmissionRow[], locations: LocationNode[]) {
  const [search, setSearch] = useState('');
  const [districtLocationId, setDistrictLocationId] = useState<number>(MINISTRY_OPTION_VALUE);
  const [unitId, setUnitId] = useState<number | undefined>();

  const districtOptions = useMemo(() => toDistrictOptions(locations), [locations]);
  const unitOptions = useMemo(() => toUnitOptions(locations, districtLocationId), [locations, districtLocationId]);

  const filteredRows = useMemo(() => {
    const selectedDistrictId = locations.find((l) => l.id === districtLocationId)?.militaryDistrictId ?? null;
    const query = search.trim().toLocaleLowerCase('uz');
    return rows.filter((r) => (selectedDistrictId === null || r.districtId === selectedDistrictId)
      && (unitId === undefined || r.unitId === unitId)
      && (query === '' || r.fullName.toLocaleLowerCase('uz').includes(query)));
  }, [rows, locations, search, districtLocationId, unitId]);

  const selectDistrict = (id: number | undefined) => { setDistrictLocationId(id ?? MINISTRY_OPTION_VALUE); setUnitId(undefined); };

  return { search, setSearch, districtLocationId, selectDistrict, unitId, setUnitId, districtOptions, unitOptions, filteredRows };
}
