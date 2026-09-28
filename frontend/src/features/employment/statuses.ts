export type CourseStatusValue = 'STUDIED' | 'EXAM_PASSED' | 'CERTIFIED' | 'DROPPED';

export const statusLabels: Record<CourseStatusValue, string> = {
  STUDIED: "O'qidi", EXAM_PASSED: "Imtihondan o'tdi", CERTIFIED: 'Sertifikat oldi', DROPPED: "O'qishni tugatmadi",
};
