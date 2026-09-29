import { SearchOutlined } from '@ant-design/icons';
import { Input, Select, Space } from 'antd';
import { admissionLabels } from '../labels';
import type { useAdmissionFilters } from '../hooks/useAdmissionFilters';

const t = admissionLabels.filters;
const FILTER_WIDTH = 260;

type Props = { filters: ReturnType<typeof useAdmissionFilters> };

export function AdmissionFilters({ filters }: Props) {
  return (
    <Space wrap>
      <Input allowClear prefix={<SearchOutlined />} placeholder={t.search} value={filters.search}
        onChange={(e) => filters.setSearch(e.target.value)} style={{ width: FILTER_WIDTH }} />
      <Select showSearch optionFilterProp="label" placeholder={t.district} value={filters.districtLocationId}
        options={filters.districtOptions} onChange={filters.selectDistrict} style={{ width: FILTER_WIDTH }} />
      <Select allowClear showSearch optionFilterProp="label" placeholder={t.allUnits} value={filters.unitId}
        options={filters.unitOptions} onChange={filters.setUnitId} style={{ width: FILTER_WIDTH }} />
    </Space>
  );
}
