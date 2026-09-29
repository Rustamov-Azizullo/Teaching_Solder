import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { groupApi, teacherApi } from '../api/groupApi';
import type { ContractRequest, GroupRequest, GroupType, InstitutionRequest, TeacherRequest } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useGroups(type?: GroupType) {
  return useQuery({ queryKey: [...queryKeys.groups, 'list', type], queryFn: () => groupApi.list(type) });
}

export function useGroup(id: number) {
  return useQuery({ queryKey: [...queryKeys.groups, id], queryFn: () => groupApi.get(id) });
}

/** Shu guruhdan tashqari, boshqa guruhlarda turgan askarlar id lari. */
export function useSoldiersInOtherGroups(id: number) {
  return useQuery({ queryKey: [...queryKeys.groups, id, 'soldiers-in-other-groups'], queryFn: () => groupApi.soldiersInOtherGroups(id) });
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

export const useDeleteGroup = () => useGroupMutation((id: number) => groupApi.remove(id));

export const useRemoveLeader = (id: number) => useGroupMutation(() => groupApi.removeLeader(id));

export const useReplaceMembers = (id: number) =>
  useGroupMutation((soldierIds: number[]) => groupApi.replaceMembers(id, soldierIds));

export const useReplaceTeachers = (id: number) =>
  useGroupMutation((teacherIds: number[]) => groupApi.replaceTeachers(id, teacherIds));

export function useTeachers() {
  return useQuery({ queryKey: ['teachers'], queryFn: teacherApi.list });
}

/** `unitId` berilsa, faqat shu harbiy qism bilan shartnomasi bor muassasalar; `enabled: false` — qism tanlanmaguncha so'ralmaydi. */
export function useInstitutions(unitId?: number, enabled = true) {
  return useQuery({ queryKey: ['institutions', unitId ?? null], queryFn: () => teacherApi.institutions(unitId), enabled });
}

export function useSaveTeacher(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: TeacherRequest) => (id === undefined ? teacherApi.create(request) : teacherApi.update(id, request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['teachers'] }),
  });
}

export function useSaveInstitution(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: InstitutionRequest) =>
      id === undefined ? teacherApi.createInstitution(request) : teacherApi.updateInstitution(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['institutions'] }),
  });
}

export const useContracts = () => useQuery({ queryKey: ['institution-contracts'], queryFn: teacherApi.contracts });

function useContractMutation<TVariables>(fn: (variables: TVariables) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => Promise.all([
      queryClient.invalidateQueries({ queryKey: ['institution-contracts'] }),
      queryClient.invalidateQueries({ queryKey: ['institutions'] }),
    ]),
  });
}

export const useCreateContract = () => useContractMutation(teacherApi.createContract);
export const useUpdateContract = () =>
  useContractMutation(({ current, next }: { current: ContractRequest; next: ContractRequest }) => teacherApi.updateContract(current, next));
export const useDeleteContract = () => useContractMutation(teacherApi.removeContract);

export function useDeleteInstitution() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => teacherApi.removeInstitution(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['institutions'] }),
  });
}

export function useDeleteTeacher() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => teacherApi.remove(id),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['teachers'] }),
        queryClient.invalidateQueries({ queryKey: queryKeys.groups }),
      ]),
  });
}
