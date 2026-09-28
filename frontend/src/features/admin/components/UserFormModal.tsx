import { Form, Input, Modal, Select, Switch } from 'antd';
import { useEffect } from 'react';
import type { Role } from '@/features/auth';
import { useMilitaryDistricts, useMilitaryUnits } from '@/features/organization';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { adminLabels } from '../labels';
import { useRoles, useSaveUser } from '../hooks/useAdmin';
import type { CreateUserRequest, UpdateUserRequest, UserRow } from '../types';
import { notify } from '@/lib/notify';

type FormValues = {
  username?: string; password?: string; newPassword?: string; fullName: string; role: Role;
  militaryDistrictId?: number; militaryUnitId?: number; active: boolean;
};

function toCreateRequest(values: FormValues): CreateUserRequest {
  return {
    username: values.username ?? '',
    password: values.password ?? '',
    fullName: values.fullName,
    role: values.role,
    militaryDistrictId: values.militaryDistrictId,
    militaryUnitId: values.militaryUnitId,
  };
}

function toUpdateRequest(values: FormValues): UpdateUserRequest {
  return {
    fullName: values.fullName,
    role: values.role,
    militaryDistrictId: values.militaryDistrictId,
    militaryUnitId: values.militaryUnitId,
    active: values.active,
    newPassword: values.newPassword || undefined,
  };
}

const required = [{ required: true, message: common.fields.required }];
const u = adminLabels.users;

export function UserFormModal({ user, open, onClose }: { user: UserRow | null; open: boolean; onClose: () => void }) {
  const [form] = Form.useForm<FormValues>();
  const role = Form.useWatch('role', form);
  const { data: roles = [] } = useRoles();
  const { data: districts = [] } = useMilitaryDistricts();
  const { data: units = [] } = useMilitaryUnits();
  const { mutateAsync, isPending } = useSaveUser(user?.id);
  const scopeLevel = roles.find((option) => option.code === role)?.scopeLevel;

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue(
      user
        ? {
            fullName: user.fullName, role: user.role, active: user.active,
            militaryDistrictId: user.militaryDistrictId ?? undefined,
            militaryUnitId: user.militaryUnitId ?? undefined,
          }
        : { active: true },
    );
  }, [open, user, form]);

  const handleOk = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values) return;
    try {
      await mutateAsync(user ? toUpdateRequest(values) : toCreateRequest(values));
      notify.success(common.states.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={user ? u.editTitle : u.createTitle} onOk={handleOk} onCancel={onClose}
      confirmLoading={isPending} okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form
        form={form}
        layout="vertical"
        onValuesChange={(changed) => {
          if ('role' in changed) form.setFieldsValue({ militaryDistrictId: undefined, militaryUnitId: undefined });
        }}
      >
        {!user && (
          <>
            <Form.Item name="username" label={u.username} rules={[...required, { min: 3 }]}><Input autoComplete="off" /></Form.Item>
            <Form.Item name="password" label={u.password} extra={u.passwordHint} rules={[...required, { min: 8 }]}>
              <Input.Password autoComplete="new-password" />
            </Form.Item>
          </>
        )}
        <Form.Item name="fullName" label={common.fields.fullName} rules={required}><Input /></Form.Item>
        <Form.Item name="role" label={u.role} rules={required}>
          <Select options={roles.map((option) => ({ value: option.code, label: option.label }))} />
        </Form.Item>
        {scopeLevel === 'DISTRICT' && (
          <Form.Item name="militaryDistrictId" label={u.district} rules={required}>
            <Select options={districts.map((d) => ({ value: d.id, label: d.name }))} />
          </Form.Item>
        )}
        {scopeLevel === 'UNIT' && (
          <Form.Item name="militaryUnitId" label={u.unit} rules={required}>
            <Select options={units.map((unit) => ({ value: unit.id, label: unit.name }))} />
          </Form.Item>
        )}
        {user && (
          <>
            <Form.Item name="newPassword" label={u.newPassword} rules={[{ min: 8 }]}><Input.Password autoComplete="new-password" /></Form.Item>
            <Form.Item name="active" label={common.fields.active} valuePropName="checked"><Switch /></Form.Item>
          </>
        )}
      </Form>
    </Modal>
  );
}
