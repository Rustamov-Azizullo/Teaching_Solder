import { PageHeader } from '@/components/ui';
import { DeadlineTable, deadlineLabels } from '@/features/deadlines';

export function DeadlinesPage() {
  return (
    <>
      <PageHeader title={deadlineLabels.title} subtitle={deadlineLabels.subtitle} />
      <DeadlineTable />
    </>
  );
}
