import { AttendanceSheetForm } from '@/features/attendance';
import { useIdParam } from '@/hooks/useIdParam';

export function AttendanceSheetPage() {
  return <AttendanceSheetForm lessonId={useIdParam('lessonId')} />;
}
