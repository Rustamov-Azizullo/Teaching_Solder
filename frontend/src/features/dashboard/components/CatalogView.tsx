import { Empty, Tabs } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { useCatalog } from '../hooks/useDashboard';
import { dashboardLabels } from '../labels';
import type { CatalogUnit } from '../types';
import { toDirectionRows, toEntryRows, type CatalogRow } from '../utils/catalogRows';
import { KpiGrid } from './KpiGrid';

const t = dashboardLabels.catalog;

const nameColumn: ColumnsType<CatalogRow>[number] = { title: t.name, dataIndex: 'name' };
const groupsColumn: ColumnsType<CatalogRow>[number] = { title: t.groups, dataIndex: 'groups', width: 100, align: 'right' };
const soldiersColumn: ColumnsType<CatalogRow>[number] = { title: t.soldiers, dataIndex: 'soldiers', width: 100, align: 'right' };
function CatalogTable({ rows, withStats }: { rows: CatalogRow[]; withStats: boolean }) {
  const columns = [nameColumn, ...(withStats ? [groupsColumn, soldiersColumn] : [])];
  return (
    <NumberedTable<CatalogRow> rowKey="name" size="small" columns={columns} dataSource={rows}
      locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} /> }} />
  );
}

function CatalogTabs({ units }: { units: CatalogUnit[] }) {
  return (
    <Tabs items={[
      { key: 'directions', label: t.directions, children: <CatalogTable rows={toDirectionRows(units)} withStats={false} /> },
      { key: 'subjects', label: t.subjects, children: <CatalogTable rows={toEntryRows(units, (unit) => unit.subjects)} withStats /> },
      { key: 'professions', label: t.professions, children: <CatalogTable rows={toEntryRows(units, (unit) => unit.professions)} withStats /> },
    ]} />
  );
}

/** Admin va User uchun: o'z doirasidagi harbiy qismlarda mavjud kasb yo'nalishlari, fanlar va kasblar jadvallari. */
export function CatalogView() {
  const { data, isLoading, error, refetch } = useCatalog();
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(catalog) => (
        <>
          <KpiGrid items={[
            { title: t.directions, value: catalog.counts.directions },
            { title: t.subjects, value: catalog.counts.subjects },
            { title: t.professions, value: catalog.counts.professions },
          ]} />
          <CatalogTabs units={catalog.units} />
        </>
      )}
    </QueryBoundary>
  );
}
