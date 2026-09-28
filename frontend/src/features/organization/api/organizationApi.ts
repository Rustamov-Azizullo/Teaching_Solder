import { apiClient } from '@/lib/apiClient';
import type { MilitaryDistrict, MilitaryUnit, Region, SubdivisionNode, TerritorialDistrict } from '../types';

export const organizationApi = {
  regions: () => apiClient.get<Region[]>('/regions').then((r) => r.data),
  districts: (regionId: number) =>
    apiClient.get<TerritorialDistrict[]>(`/regions/${regionId}/districts`).then((r) => r.data),
  militaryDistricts: () => apiClient.get<MilitaryDistrict[]>('/military-districts').then((r) => r.data),
  subdivisions: (unitId: number) =>
    apiClient.get<SubdivisionNode[]>(`/military-units/${unitId}/subdivisions`).then((r) => r.data),
  createSubdivision: (unitId: number, name: string, parentId?: number) =>
    apiClient.post<SubdivisionNode>(`/military-units/${unitId}/subdivisions`, { name, parentId }).then((r) => r.data),
  updateSubdivision: (id: number, name: string, parentId: number | null) =>
    apiClient.put<SubdivisionNode>(`/subdivisions/${id}`, { name, parentId }).then((r) => r.data),
  deleteSubdivision: (id: number) => apiClient.delete(`/subdivisions/${id}`).then(() => undefined),
  militaryUnits: () => apiClient.get<MilitaryUnit[]>('/military-units').then((r) => r.data),
};
