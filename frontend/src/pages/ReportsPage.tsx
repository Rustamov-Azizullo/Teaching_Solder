import { PageHeader } from '@/components/ui';
import { ReportBuilder, reportLabels } from '@/features/reports';

export function ReportsPage() {
  return (
    <>
      <PageHeader title={reportLabels.title} subtitle={reportLabels.subtitle} />
      <ReportBuilder />
    </>
  );
}
