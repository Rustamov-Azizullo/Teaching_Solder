import { PageHeader } from '@/components/ui';
import { EmploymentLists, employmentLabels } from '@/features/employment';

export function EmploymentPage() {
  return (
    <>
      <PageHeader title={employmentLabels.title} subtitle={employmentLabels.subtitle} />
      <EmploymentLists />
    </>
  );
}
