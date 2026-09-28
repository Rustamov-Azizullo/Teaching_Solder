import { PageHeader } from '@/components/ui';
import { UsersManager, adminLabels } from '@/features/admin';

export function UsersPage() {
  return (
    <>
      <PageHeader title={adminLabels.users.title} subtitle={adminLabels.users.subtitle} />
      <UsersManager />
    </>
  );
}
