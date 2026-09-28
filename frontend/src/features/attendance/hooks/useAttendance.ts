import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { attendanceApi } from '../api/attendanceApi';
import type { AttendanceRequest } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useAttendanceSheet(lessonId: number) {
  return useQuery({ queryKey: [...queryKeys.attendance, lessonId], queryFn: () => attendanceApi.sheet(lessonId) });
}

export function useRecordAttendance(lessonId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: AttendanceRequest) => attendanceApi.record(lessonId, request),
    onSuccess: (sheet) => {
      queryClient.setQueryData([...queryKeys.attendance, lessonId], sheet);
      queryClient.invalidateQueries({ queryKey: queryKeys.lessons });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}
