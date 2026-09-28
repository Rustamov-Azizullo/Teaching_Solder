import { apiClient } from '@/lib/apiClient';
import type { GenerateLessonsRequest, Lesson, LessonRequest } from '../types';

export const lessonApi = {
  forGroup: (groupId: number, from: string, to: string) =>
    apiClient.get<Lesson[]>(`/groups/${groupId}/lessons`, { params: { from, to } }).then((r) => r.data),
  forDate: (date: string) => apiClient.get<Lesson[]>('/lessons', { params: { date } }).then((r) => r.data),
  create: (groupId: number, request: LessonRequest) =>
    apiClient.post<Lesson>(`/groups/${groupId}/lessons`, request).then((r) => r.data),
  generate: (groupId: number, request: GenerateLessonsRequest) =>
    apiClient.post<Lesson[]>(`/groups/${groupId}/lessons/generate`, request).then((r) => r.data),
  cancel: (lessonId: number, reason: string) =>
    apiClient.post<Lesson>(`/lessons/${lessonId}/cancel`, { reason }).then((r) => r.data),
};
