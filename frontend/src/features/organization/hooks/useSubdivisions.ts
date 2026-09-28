import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { organizationApi } from '../api/organizationApi';

export function useSubdivisions(unitId: number | undefined) {
  return useQuery({
    queryKey: ['subdivisions', unitId],
    queryFn: () => organizationApi.subdivisions(unitId as number),
    enabled: unitId !== undefined,
  });
}

export function useSubdivisionMutations(unitId: number) {
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['subdivisions', unitId] });
  return {
    create: useMutation({
      mutationFn: ({ name, parentId }: { name: string; parentId?: number }) => organizationApi.createSubdivision(unitId, name, parentId),
      onSuccess: invalidate,
    }),
    rename: useMutation({
      mutationFn: ({ id, name, parentId }: { id: number; name: string; parentId: number | null }) =>
        organizationApi.updateSubdivision(id, name, parentId),
      onSuccess: invalidate,
    }),
    remove: useMutation({ mutationFn: (id: number) => organizationApi.deleteSubdivision(id), onSuccess: invalidate }),
  };
}
