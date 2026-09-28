import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { assignmentApi } from '../api/assignmentApi';
import type { ContractRequest, DecisionRequest, ProposalRequest } from '../types';

const KEY = ['assignments'];

export const useAssignments = () => useQuery({ queryKey: KEY, queryFn: assignmentApi.list });

function useAssignmentMutation<TVariables, TResult>(fn: (v: TVariables) => Promise<TResult>) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: fn, onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }) });
}

export const usePropose = () => useAssignmentMutation((r: ProposalRequest) => assignmentApi.propose(r));
export const useReview = () => useAssignmentMutation((id: number) => assignmentApi.review(id));
export const useDecide = () => useAssignmentMutation(({ id, request }: { id: number; request: DecisionRequest }) => assignmentApi.decide(id, request));
export const useContract = () => useAssignmentMutation(({ id, request }: { id: number; request: ContractRequest }) => assignmentApi.contract(id, request));
