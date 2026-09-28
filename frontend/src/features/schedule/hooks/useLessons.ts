import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { lessonApi } from '../api/lessonApi';
import type { GenerateLessonsRequest, LessonRequest } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useGroupLessons(groupId: number, from: string, to: string) {
  return useQuery({ queryKey: [...queryKeys.lessons, 'group', groupId, from, to], queryFn: () => lessonApi.forGroup(groupId, from, to) });
}

export function useLessonsByDate(date: string) {
  return useQuery({ queryKey: [...queryKeys.lessons, 'date', date], queryFn: () => lessonApi.forDate(date) });
}

function useLessonMutation<TVariables, TResult>(mutationFn: (variables: TVariables) => Promise<TResult>) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn, onSuccess: () => Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.lessons }),
        queryClient.invalidateQueries({ queryKey: queryKeys.attendance }),
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboard }),
      ]) });
}

export const useCreateLesson = (groupId: number) => useLessonMutation((request: LessonRequest) => lessonApi.create(groupId, request));

export const useGenerateLessons = (groupId: number) =>
  useLessonMutation((request: GenerateLessonsRequest) => lessonApi.generate(groupId, request));

export const useCancelLesson = () =>
  useLessonMutation(({ lessonId, reason }: { lessonId: number; reason: string }) => lessonApi.cancel(lessonId, reason));
