import { PageHeader } from '@/components/ui';
import { TeacherManager, groupLabels } from '@/features/groups';

export function TeachersPage() {
  return (
    <>
      <PageHeader title={groupLabels.teachers.title} subtitle={groupLabels.teachers.subtitle} />
      <TeacherManager />
    </>
  );
}
