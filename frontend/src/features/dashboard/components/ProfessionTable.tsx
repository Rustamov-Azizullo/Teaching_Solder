import { SolutionOutlined } from '@ant-design/icons';
import { ChartCard, NumberedTable } from '@/components/ui';
import { dashboardLabels } from '../labels';
import type { DistrictSoldiers, ProfessionRow, SubdivisionSoldiers } from '../types';

const t = dashboardLabels;

type PlacementRow = { key: string; district: string; unit: string; soldiers: number };

const toPlacements = (districts: DistrictSoldiers[]): PlacementRow[] =>
  districts.flatMap((district) => district.units.map((unit) => ({
    key: `${district.id}-${unit.id}`, district: district.name, unit: unit.name, soldiers: unit.soldiers,
  })));

function PlacementTable({ districts }: { districts: DistrictSoldiers[] }) {
  return (
    <NumberedTable<PlacementRow> rowKey="key" size="small" pagination={false} dataSource={toPlacements(districts)}
      columns={[
        { title: t.district, dataIndex: 'district' },
        { title: t.unit, dataIndex: 'unit' },
        { title: t.soldiers, dataIndex: 'soldiers', width: 90, align: 'right' },
      ]} />
  );
}

function SubdivisionTable({ subdivisions }: { subdivisions: SubdivisionSoldiers[] }) {
  return (
    <NumberedTable<SubdivisionSoldiers> rowKey="name" size="small" pagination={false} dataSource={subdivisions}
      columns={[
        { title: t.subdivision, dataIndex: 'name' },
        { title: t.soldiers, dataIndex: 'soldiers', width: 90, align: 'right' },
      ]} />
  );
}

/**
 * Kasblar kesimida askarlar; qatorni ochsangiz — qaysi okrug va harbiy qismga tegishli ekani ko'rinadi.
 * Qism darajasidagi foydalanuvchi (`bySubdivision`) uchun esa o'z bo'linmalari kesimi ko'rsatiladi.
 */
export function ProfessionTable({ rows, bySubdivision }: { rows: ProfessionRow[]; bySubdivision: boolean }) {
  const renderDetails = (row: ProfessionRow) => bySubdivision
    ? <SubdivisionTable subdivisions={row.subdivisions} />
    : <PlacementTable districts={row.districts} />;
  return (
    <ChartCard title={t.professions} icon={<SolutionOutlined />} isEmpty={rows.length === 0}>
      <NumberedTable<ProfessionRow> rowKey="profession" size="small" pagination={false} dataSource={rows}
        scroll={{ y: 'max-content' }} style={{ height: '100%', overflow: 'auto' }}
        expandable={{ expandedRowRender: renderDetails }}
        columns={[
          { title: t.profession, dataIndex: 'profession' },
          { title: t.soldiers, dataIndex: 'soldiers', width: 90, align: 'right' },
        ]} />
    </ChartCard>
  );
}
