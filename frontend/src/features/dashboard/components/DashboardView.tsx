import { ReloadOutlined } from '@ant-design/icons';
import { Button, DatePicker, Space, Tabs, Tag, type TabsProps } from 'antd';
import type { Dayjs } from 'dayjs';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { AdmissionFunnel } from '@/features/admissions';
import { useCan } from '@/features/auth';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { useAttendanceBlock, useSurveyBlock } from '../hooks/useDashboard';
import { useDashboardFilters } from '../hooks/useDashboardFilters';
import { dashboardLabels } from '../labels';
import type { AttendanceKind } from '../types';
import { AttendanceBlockView } from './AttendanceBlockView';
import { CourseResultsChart } from './CourseResultsChart';
import { SurveyBlockView } from './SurveyBlockView';

function AttendanceTab({ kind, enabled, filtersHook }: {
  kind: AttendanceKind;
  enabled: boolean;
  filtersHook: ReturnType<typeof useDashboardFilters>;
}) {
  const { filters, drillInto } = filtersHook;
  const { data, isLoading, error, refetch } = useAttendanceBlock(kind, filters, enabled);
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(block) => (
        <>
          <AttendanceBlockView block={block} onDrill={(row) => drillInto(block.breakdown.level, row)} />
          <div style={{ marginTop: 12 }}>
            {kind === 'vocational' ? <CourseResultsChart filters={filters} /> : <AdmissionFunnel />}
          </div>
        </>
      )}
    </QueryBoundary>
  );
}

export function DashboardView() {
  const canVocational = useCan('dashboardVocational');
  const canOtm = useCan('dashboardOtm');
  const canSurveys = useCan('dashboardSurveys');
  const filtersHook = useDashboardFilters();
  const { filters, drillLabels, setPeriod, reset } = filtersHook;
  const surveys = useSurveyBlock({ districtId: filters.districtId, unitId: filters.unitId }, canSurveys);
  const isDrilled = filters.districtId !== undefined || filters.unitId !== undefined;

  const tabs: NonNullable<TabsProps['items']> = [];
  if (canVocational) {
    tabs.push({ key: 'vocational', label: dashboardLabels.blocks.vocational, children: <AttendanceTab kind="vocational" enabled filtersHook={filtersHook} /> });
  }
  if (canOtm) {
    tabs.push({ key: 'otm', label: dashboardLabels.blocks.otm, children: <AttendanceTab kind="otm" enabled filtersHook={filtersHook} /> });
  }
  if (canSurveys) {
    tabs.push({
      key: 'surveys',
      label: dashboardLabels.blocks.surveys,
      children: (
        <QueryBoundary isLoading={surveys.isLoading} error={surveys.error} data={surveys.data} onRetry={surveys.refetch}>
          {(block) => <SurveyBlockView block={block} />}
        </QueryBoundary>
      ),
    });
  }

  return (
    <>
      <PageHeader
        title={dashboardLabels.title}
        subtitle={dashboardLabels.subtitle}
        actions={[
          <DatePicker.RangePicker
            key="range"
            allowClear={false}
            format={DISPLAY_DATE_FORMAT}
            value={[dayjs(filters.from), dayjs(filters.to)]}
            onChange={(value: [Dayjs | null, Dayjs | null] | null) =>
              value?.[0] && value[1] && setPeriod(value[0].format(API_DATE_FORMAT), value[1].format(API_DATE_FORMAT))
            }
          />,
        ]}
      />
      {isDrilled && (
        <Space style={{ marginBottom: 12 }}>
          {filters.districtId !== undefined && <Tag color="blue">{drillLabels.district ?? dashboardLabels.drill.district}</Tag>}
          {filters.unitId !== undefined && <Tag color="geekblue">{drillLabels.unit ?? dashboardLabels.drill.unit}</Tag>}
          <Button size="small" icon={<ReloadOutlined />} onClick={reset}>{dashboardLabels.drill.reset}</Button>
        </Space>
      )}
      <Tabs items={tabs} />
    </>
  );
}
