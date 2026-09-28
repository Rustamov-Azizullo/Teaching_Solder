import { PageHeader } from '@/components/ui';
import { AdmissionTable, admissionLabels } from '@/features/admissions';

export function AdmissionsPage() {
  return (
    <>
      <PageHeader title={admissionLabels.title} subtitle={admissionLabels.subtitle} />
      <AdmissionTable />
    </>
  );
}
