import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { groupApi, teacherApi } from '../api/groupApi';
import type { GroupRequest, GroupType, InstitutionRequest, LeaderRequest, TeacherRequest } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useGroups(type?: GroupType) {
  return useQuery({ queryKey: [...queryKeys.groups, 'list', type], queryFn: () => groupApi.list(type) });
}

export function useGroup(id: number) {
  return useQuery({ queryKey: [...queryKeys.groups, id], queryFn: () => groupApi.get(id) });
}

/** Guruh o'zgarganda ro'yxat va tafsilot keshini yangilaydi. */
function useGroupMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.groups }),
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboard }),
      ]),
  });
}

export const useCreateGroup = () => useGroupMutation((request: GroupRequest) => groupApi.create(request));

export const useUpdateGroup = (id: number) => useGroupMutation((request: GroupRequest) => groupApi.update(id, request));

export const useAssignLeader = (id: number) =>
  useGroupMutation((request: LeaderRequest) => groupApi.assignLeader(id, request));

export const useReplaceMembers = (id: number) =>
  useGroupMutation((soldierIds: number[]) => groupApi.replaceMembers(id, soldierIds));

export const useReplaceTeachers = (id: number) =>
  useGroupMutation((teacherIds: number[]) => groupApi.replaceTeachers(id, teacherIds));

export function useLeaderOptions(unitId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: ['group-leaders', unitId],
    queryFn: () => groupApi.leaderOptions(unitId as number),
    enabled: enabled && unitId !== undefined,
  });
}

export function useTeachers() {
  return useQuery({ queryKey: ['teachers'], queryFn: teacherApi.list });
}

export function useInstitutions() {
  return useQuery({ queryKey: ['institutions'], queryFn: teacherApi.institutions });
}

export function useSaveTeacher(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: TeacherRequest) => (id === undefined ? teacherApi.create(request) : teacherApi.update(id, request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['teachers'] }),
  });
}

export function useCreateInstitution() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: InstitutionRequest) => teacherApi.createInstitution(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['institutions'] }),
  });
}
