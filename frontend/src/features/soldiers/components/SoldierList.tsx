import { PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, Input, Space, Table } from 'antd';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { MilitaryUnitSelect, SubdivisionTreeSelect } from '@/features/organization';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { formatDate } from '@/utils/format';
import { soldierLabels } from '../labels';
import { useSoldierSearch } from '../hooks/useSoldiers';
import { SoldierImportModal } from './SoldierImportModal';
import type { SoldierSummary } from '../types';

const PAGE_SIZE = 20;

export function SoldierList() {
  const navigate = useNavigate();
  const canCreate = useCan('soldierWrite');
  const [query, setQuery] = useState('');
  const [unitId, setUnitId] = useState<number | undefined>();
  const [subdivisionId, setSubdivisionId] = useState<number | undefined>();
  const [isImportOpen, setImportOpen] = useState(false);
  const [page, setPage] = useState(0);
  const debouncedQuery = useDebouncedValue(query);
  const { data, isLoading, isFetching, error, refetch } = useSoldierSearch({
    query: debouncedQuery,
    unitId,
    subdivisionId,
    page,
    size: PAGE_SIZE,
  });

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap>
        <Input.Search
          allowClear
          placeholder={soldierLabels.searchPlaceholder}
          style={{ width: 280 }}
          onChange={(event) => { setQuery(event.target.value); setPage(0); }}
        />
        <MilitaryUnitSelect allowClear value={unitId} onChange={(value) => { setUnitId(value); setSubdivisionId(undefined); setPage(0); }} />
        <SubdivisionTreeSelect unitId={unitId} value={subdivisionId} placeholder={soldierLabels.subdivisionFilter} onChange={(value) => { setSubdivisionId(value); setPage(0); }} />
        {canCreate && (
          <>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/soldiers/new')}>
              {soldierLabels.create}
            </Button>
            <Button icon={<UploadOutlined />} onClick={() => setImportOpen(true)}>{soldierLabels.import.button}</Button>
          </>
        )}
      </Space>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(result) => (
          <Table<SoldierSummary>
            rowKey="id"
            loading={isFetching}
            dataSource={result.content}
            scroll={{ x: 'max-content' }}
            pagination={{
              current: result.page + 1,
              pageSize: PAGE_SIZE,
              total: result.totalElements,
              showSizeChanger: false,
              onChange: (nextPage) => setPage(nextPage - 1),
            }}
            columns={[
              {
                title: 'F.I.Sh.',
                dataIndex: 'fullName',
                render: (name: string, row) => <Link to={`/soldiers/${row.id}`}>{name}</Link>,
              },
              { title: soldierLabels.columns.pinfl, dataIndex: 'pinfl' },
              { title: soldierLabels.columns.birthDate, dataIndex: 'birthDate', render: formatDate },
              { title: soldierLabels.columns.unit, dataIndex: 'militaryUnitName' },
              { title: soldierLabels.columns.subdivision, dataIndex: 'subdivisionPath', render: (v: string | null) => v ?? '—' },
              {
                title: soldierLabels.columns.address,
                render: (_: unknown, row) => `${row.regionName}, ${row.districtName}`,
              },
            ]}
          />
        )}
      </QueryBoundary>
      <SoldierImportModal open={isImportOpen} onClose={() => setImportOpen(false)} />
    </Space>
  );
}
