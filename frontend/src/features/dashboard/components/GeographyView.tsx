import { Col, Row, Table, Tag, Typography } from 'antd';
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartCard, KpiCard } from '@/components/ui';
import { CHART_HEIGHT, chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import type { CountItem, GeographyBlock, InstitutionContracts, RegionInstitutions } from '../types';
import { DistrictSoldiersCharts } from './DistrictSoldiersCharts';
import { HorizontalCountChart } from './SurveyCharts';

const labels = dashboardLabels.geography;
const APPROVED_STATUS = 'APPROVED';

function InstitutionsChart({ regions }: { regions: RegionInstitutions[] }) {
  const data = regions.filter((region) => region.institutionCount > 0);
  return (
    <ChartCard title={dashboardLabels.charts.institutionsByRegion} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT + 40}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="region" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={60} />
          <YAxis allowDecimals={false} />
          <Tooltip />
          <Legend verticalAlign="top" />
          <Bar dataKey="institutionCount" name={dashboardLabels.charts.institutions} fill={chartColors.primary} />
          <Bar dataKey="contractCount" name={dashboardLabels.charts.contracts} fill={chartColors.secondary} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

function InstitutionRows({ institutions }: { institutions: InstitutionContracts[] }) {
  return (
    <Table<InstitutionContracts>
      rowKey="id"
      size="small"
      pagination={false}
      dataSource={institutions}
      columns={[
        { title: labels.institution, dataIndex: 'name' },
        { title: labels.type, dataIndex: 'type', width: 140, render: (type: string) => labels.types[type] ?? type },
        {
          title: labels.units,
          dataIndex: 'units',
          render: (units: InstitutionContracts['units']) =>
            units.length === 0 ? (
              <Typography.Text type="secondary">{labels.noContracts}</Typography.Text>
            ) : (
              units.map((unit) => (
                <Tag key={unit.unitId} color={unit.status === APPROVED_STATUS ? 'green' : 'gold'} style={{ marginBottom: 4 }}>
                  {unit.unitName} · {labels.statuses[unit.status] ?? unit.status}
                  {unit.contractNo ? ` · ${labels.contractNo(unit.contractNo)}` : ''}
                </Tag>
              ))
            ),
        },
      ]}
    />
  );
}

function RegionsTable({ regions }: { regions: RegionInstitutions[] }) {
  return (
    <ChartCard title={labels.institutionsTable} isEmpty={regions.length === 0}>
      <Table<RegionInstitutions>
        rowKey="region"
        size="small"
        pagination={false}
        dataSource={regions}
        scroll={{ x: 'max-content' }}
        expandable={{
          rowExpandable: (region) => region.institutionCount > 0,
          expandedRowRender: (region) => <InstitutionRows institutions={region.institutions} />,
        }}
        columns={[
          { title: labels.region, dataIndex: 'region' },
          { title: labels.soldiers, dataIndex: 'soldiers', width: 110 },
          { title: dashboardLabels.kpi.institutions, dataIndex: 'institutionCount', width: 120 },
          { title: dashboardLabels.kpi.contracts, dataIndex: 'contractCount', width: 120 },
        ]}
      />
    </ChartCard>
  );
}

type GeographyViewProps = { block: GeographyBlock; professionChoices: CountItem[] };

export function GeographyView({ block, professionChoices }: GeographyViewProps) {
  const institutionTotal = block.regions.reduce((sum, region) => sum + region.institutionCount, 0);
  const contractTotal = block.regions.reduce((sum, region) => sum + region.contractCount, 0);
  return (
    <>
      <Row gutter={[12, 12]} style={{ marginBottom: 12 }}>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.soldiers} value={block.totalSoldiers} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.districts} value={block.districts.length} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.institutions} value={institutionTotal} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.contracts} value={contractTotal} /></Col>
      </Row>
      <div style={{ marginBottom: 12 }}><DistrictSoldiersCharts districts={block.districts} /></div>
      <Row gutter={[12, 12]}>
        <Col xs={24} xl={12}><InstitutionsChart regions={block.regions} /></Col>
        <Col xs={24} xl={12}><HorizontalCountChart title={dashboardLabels.charts.professionChoices} data={professionChoices} /></Col>
        <Col span={24}><RegionsTable regions={block.regions} /></Col>
      </Row>
    </>
  );
}
