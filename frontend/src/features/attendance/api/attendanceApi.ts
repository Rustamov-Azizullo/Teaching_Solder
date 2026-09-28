import { apiClient } from '@/lib/apiClient';
import type { AttendanceRequest, AttendanceSheet } from '../types';

export const attendanceApi = {
  sheet: (lessonId: number) => apiClient.get<AttendanceSheet>(`/lessons/${lessonId}/attendance`).then((r) => r.data),
  record: (lessonId: number, request: AttendanceRequest) =>
    apiClient.put<AttendanceSheet>(`/lessons/${lessonId}/attendance`, request).then((r) => r.data),
};
