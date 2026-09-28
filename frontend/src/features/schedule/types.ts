export type LessonKind = 'THEORY' | 'PRACTICAL';
export type LessonStatus = 'PLANNED' | 'HELD' | 'CANCELLED';

export type Lesson = {
  id: number;
  groupId: number;
  groupName: string;
  groupType: 'VOCATIONAL' | 'OTM_PREP';
  lessonDate: string;
  startTime: string;
  endTime: string;
  academicHours: number;
  topic: string | null;
  kind: LessonKind;
  status: LessonStatus;
  changeReason: string | null;
  teacherPresent: boolean | null;
  attendanceRecorded: boolean;
};

export type LessonRequest = {
  lessonDate: string;
  startTime?: string;
  endTime?: string;
  academicHours?: number;
  topic?: string;
  kind: LessonKind;
  changeReason?: string;
};

export type GenerateLessonsRequest = { from: string; to: string; kind: LessonKind };
