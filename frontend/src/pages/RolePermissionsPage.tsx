import { PageHeader } from '@/components/ui';
import { RolePermissionsMatrix, rolePermissionsLabels } from '@/features/rolePermissions';

export function RolePermissionsPage() {
  return (
    <>
      <PageHeader title={rolePermissionsLabels.title} subtitle={rolePermissionsLabels.subtitle} />
      <RolePermissionsMatrix />
    </>
  );
}
