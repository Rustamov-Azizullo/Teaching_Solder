import { PageHeader } from '@/components/ui';
import { IntegrationLogTable, adminLabels } from '@/features/admin';

export function IntegrationLogsPage() {
  return (
    <>
      <PageHeader title={adminLabels.integrations.title} subtitle={adminLabels.integrations.subtitle} />
      <IntegrationLogTable />
    </>
  );
}
