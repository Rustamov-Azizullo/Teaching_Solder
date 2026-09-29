import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { dictionaryApi } from '../api/dictionaryApi';
import type { DictionaryItemInput, DictionaryType } from '../types';

const REFERENCE_STALE_TIME_MS = 60_000;

export function useDictionaryTypes() {
  return useQuery({ queryKey: ['dictionary-types'], queryFn: dictionaryApi.types, staleTime: REFERENCE_STALE_TIME_MS });
}

export function useDictionary(type: DictionaryType, options: { activeOnly?: boolean; unitId?: number } = {}) {
  return useQuery({
    queryKey: ['dictionary', type, options],
    queryFn: () => dictionaryApi.list(type, options),
    staleTime: REFERENCE_STALE_TIME_MS,
  });
}

export function useSaveDictionaryItem(type: DictionaryType) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: DictionaryItemInput }) =>
      id === undefined ? dictionaryApi.create(type, input) : dictionaryApi.update(type, id, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dictionary', type] }),
  });
}

export function useDeleteDictionaryItem(type: DictionaryType) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => dictionaryApi.remove(type, id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dictionary', type] }),
  });
}

export function useUnitDirections(unitId: number | undefined) {
  return useQuery({
    queryKey: ['unit-directions', unitId],
    queryFn: () => dictionaryApi.unitDirections(unitId as number),
    enabled: unitId !== undefined,
  });
}

export function useReplaceUnitDirections(unitId: number | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (directionIds: number[]) => dictionaryApi.replaceUnitDirections(unitId as number, directionIds),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['unit-directions', unitId] });
      queryClient.invalidateQueries({ queryKey: ['dictionary', 'PROFESSION_DIRECTION'] });
    },
  });
}
