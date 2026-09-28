import { PageHeader } from '@/components/ui';
import { AuditLogTable, adminLabels } from '@/features/admin';

export function AuditPage() {
  return (
    <>
      <PageHeader title={adminLabels.audit.title} subtitle={adminLabels.audit.subtitle} />
      <AuditLogTable />
    </>
  );
}
