import { apiClient } from '@/lib/apiClient';
import type { DictionaryItem, DictionaryItemInput, DictionaryType, DictionaryTypeInfo } from '../types';

type ListOptions = { activeOnly?: boolean; unitId?: number };

export const dictionaryApi = {
  types: () => apiClient.get<DictionaryTypeInfo[]>('/dictionaries').then((r) => r.data),
  list: (type: DictionaryType, { activeOnly = true, unitId }: ListOptions = {}) =>
    apiClient.get<DictionaryItem[]>(`/dictionaries/${type}`, { params: { activeOnly, unitId } }).then((r) => r.data),
  create: (type: DictionaryType, input: DictionaryItemInput) =>
    apiClient.post<DictionaryItem>(`/dictionaries/${type}`, input).then((r) => r.data),
  update: (type: DictionaryType, id: number, input: DictionaryItemInput) =>
    apiClient.put<DictionaryItem>(`/dictionaries/${type}/${id}`, input).then((r) => r.data),
  unitDirections: (unitId: number) =>
    apiClient.get<number[]>(`/military-units/${unitId}/directions`).then((r) => r.data),
  replaceUnitDirections: (unitId: number, directionIds: number[]) =>
    apiClient.put(`/military-units/${unitId}/directions`, { directionIds }),
};
