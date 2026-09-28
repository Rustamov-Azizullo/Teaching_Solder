import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { deadlineApi } from '../api/deadlineApi';
import type { DeadlineRequest } from '../types';

const KEY = ['deadlines'];
export const useDeadlines = () => useQuery({ queryKey: KEY, queryFn: deadlineApi.list });

function useRefreshing<TVariables, TResult>(fn: (v: TVariables) => Promise<TResult>) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: fn, onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }) });
}

export const useSaveDeadline = () =>
  useRefreshing(({ id, request }: { id?: number; request: DeadlineRequest }) => (id === undefined ? deadlineApi.create(request) : deadlineApi.update(id, request)));
export const useCompleteDeadline = () => useRefreshing((id: number) => deadlineApi.complete(id));
export const useProcessDeadlines = () => useRefreshing(() => deadlineApi.process());
