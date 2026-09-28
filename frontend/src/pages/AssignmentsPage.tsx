import { PageHeader } from '@/components/ui';
import { AssignmentList, assignmentLabels } from '@/features/assignments';

export function AssignmentsPage() {
  return (
    <>
      <PageHeader title={assignmentLabels.title} subtitle={assignmentLabels.subtitle} />
      <AssignmentList />
    </>
  );
}
