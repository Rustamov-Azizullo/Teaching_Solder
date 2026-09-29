import { Form, Input, Modal, Select, Switch } from 'antd';
import { useEffect } from 'react';
import { isPermissionManager, useAuth, type Role } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useRoles, useUpdateUser } from '../hooks/useAdmin';
import { adminLabels } from '../labels';
import type { UpdateUserRequest, UserRow } from '../types';
import { locationLevelForRole } from '../utils/locationOptions';
import { LocationSelect } from './LocationSelect';
import { UserPermissionsPanel } from './UserPermissionsPanel';

type FormValues = {
  username: string; newPassword?: string; fullName: string; role: Role;
  locationId?: number; active: boolean;
};

/** Vazirlik rollari hududga biriktirilmaydi — eski qiymat yuborilmasligi uchun tozalanadi. */
function locationIdFor(values: FormValues): number | undefined {
  return locationLevelForRole(values.role) ? values.locationId : undefined;
}

function toUpdateRequest(values: FormValues): UpdateUserRequest {
  return {
    username: values.username.trim(),
    fullName: values.fullName,
    role: values.role,
    locationId: locationIdFor(values),
    active: values.active,
    newPassword: values.newPassword || undefined,
  };
}

/** Shaxsiy ruxsatlar faqat sozlanadigan rollarga (ADMIN/USER) beriladi. */
const CONFIGURABLE_ROLES: readonly Role[] = ['ADMIN', 'USER'];

const required = [{ required: true, message: common.fields.required }];
const u = adminLabels.users;

type UserFormModalProps = { user: UserRow | null; onClose: () => void };

export function UserFormModal({ user, onClose }: UserFormModalProps) {
  const open = user !== null;
  const [form] = Form.useForm<FormValues>();
  const role = Form.useWatch('role', form);
  const { user: viewer, logout } = useAuth();
  const { data: roles = [] } = useRoles();
  const { mutateAsync, isPending } = useUpdateUser(user?.id ?? 0);
  const locationLevel = locationLevelForRole(role);
  const canEditPermissions = user !== null && CONFIGURABLE_ROLES.includes(user.role) && isPermissionManager(viewer);

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    if (user) form.setFieldsValue({ username: user.username, fullName: user.fullName, role: user.role, active: user.active, locationId: user.locationId ?? undefined });
  }, [open, user, form]);

  const handleOk = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values || !user) return;
    try {
      await mutateAsync(toUpdateRequest(values));
      notify.success(common.states.saved);
      onClose();
      // Sessiya login (JWT subject) bilan bog'liq: o'z loginini o'zgartirgan foydalanuvchi qayta kirishi shart.
      if (user.id === viewer?.id && values.username.trim() !== user.username) {
        notify.warning(u.selfRenamed);
        logout();
      }
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={u.editTitle} onOk={handleOk} onCancel={onClose}
      confirmLoading={isPending} okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form
        form={form}
        layout="vertical"
        onValuesChange={(changed) => {
          if ('role' in changed) form.setFieldsValue({ locationId: undefined });
        }}
      >
        <Form.Item name="username" label={u.username} rules={[...required, { min: 3, max: 60 }]}><Input autoComplete="off" /></Form.Item>
        <Form.Item name="fullName" label={common.fields.fullName} rules={required}><Input /></Form.Item>
        <Form.Item name="role" label={u.role} rules={required}>
          <Select options={roles.map((option) => ({ value: option.code, label: option.label }))} />
        </Form.Item>
        {locationLevel && (
          <Form.Item name="locationId" label={locationLevel === 'DISTRICT' ? u.district : u.unit} rules={required}>
            <LocationSelect level={locationLevel} />
          </Form.Item>
        )}
        <Form.Item name="newPassword" label={u.newPassword} rules={[{ min: 8 }]}><Input.Password autoComplete="new-password" /></Form.Item>
        <Form.Item name="active" label={common.fields.active} valuePropName="checked"><Switch /></Form.Item>
      </Form>
      {canEditPermissions && user && <UserPermissionsPanel userId={user.id} />}
    </Modal>
  );
}
