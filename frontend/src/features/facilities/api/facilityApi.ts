import { apiClient } from '@/lib/apiClient';
import type { Facility, FacilityRequest } from '../types';

export const facilityApi = {
  list: () => apiClient.get<Facility[]>('/facilities').then((r) => r.data),
  create: (request: FacilityRequest) => apiClient.post<Facility>('/facilities', request).then((r) => r.data),
  update: (id: number, request: FacilityRequest) => apiClient.put<Facility>(`/facilities/${id}`, request).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/facilities/${id}`).then(() => undefined),
};
