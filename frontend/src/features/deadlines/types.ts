import type { Role } from '@/features/auth';

export type Deadline = {
  id: number;
  name: string;
  description: string | null;
  deadlineDate: string;
  responsibleRole: Role;
  escalationRole: Role;
  remindDaysBefore: number;
  status: 'OPEN' | 'DONE';
  overdue: boolean;
  daysLeft: number;
  cycleYear: number;
};

export type DeadlineRequest = {
  name: string;
  description?: string;
  deadlineDate: string;
  responsibleRole: Role;
  escalationRole: Role;
  remindDaysBefore?: number;
};
