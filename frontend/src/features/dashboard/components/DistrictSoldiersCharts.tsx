import { useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Col, Row, Typography } from 'antd';
import { ChartCard } from '@/components/ui';
import { CHART_HEIGHT, chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import type { DistrictSoldiers } from '../types';

const CHART_LABELS = dashboardLabels.charts;
const CLICKABLE = { cursor: 'pointer' } as const;

type NamedCount = { id: number; name: string; soldiers: number };

function SoldiersBarChart({ data, selectedId, onSelect }: { data: NamedCount[]; selectedId?: number; onSelect?: (id: number) => void }) {
  return (
    <ResponsiveContainer width="100%" height={CHART_HEIGHT + 60}>
      <BarChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={60} />
        <YAxis allowDecimals={false} />
        <Tooltip />
        <Bar dataKey="soldiers" name="Askarlar" style={onSelect ? CLICKABLE : undefined} onClick={(_, index) => onSelect?.(data[index].id)}>
          {data.map((item, index) => (
            <Cell
              key={item.id}
              fill={selectedId === undefined || selectedId === item.id ? chartColors.series[index % chartColors.series.length] : chartColors.muted}
            />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

/** Okruglar kesimidagi vertikal diagramma; okrug ustunini bosganda uning harbiy qismlari diagrammasi chiziladi. */
type DistrictSoldiersChartsProps = { districts: DistrictSoldiers[]; title?: string };

export function DistrictSoldiersCharts({ districts, title = CHART_LABELS.soldiersByDistrict }: DistrictSoldiersChartsProps) {
  const [selectedId, setSelectedId] = useState<number | undefined>(undefined);
  const selected = districts.find((district) => district.id === selectedId);

  return (
    <Row gutter={[12, 12]}>
      <Col xs={24} xl={selected ? 12 : 24}>
        <ChartCard title={title} isEmpty={districts.length === 0} extra={<Typography.Text type="secondary">{CHART_LABELS.pickDistrict}</Typography.Text>}>
          <SoldiersBarChart data={districts} selectedId={selected?.id} onSelect={setSelectedId} />
        </ChartCard>
      </Col>
      {selected && (
        <Col xs={24} xl={12}>
          <ChartCard title={CHART_LABELS.unitsOfDistrict(selected.name)} isEmpty={selected.units.length === 0}>
            <SoldiersBarChart data={selected.units} />
          </ChartCard>
        </Col>
      )}
    </Row>
  );
}
