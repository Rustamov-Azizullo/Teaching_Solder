import { Form, Input, Select } from 'antd';
import { useEffect } from 'react';
import type { LocationLevel } from '@/features/auth';
import { common } from '@/lib/i18n';
import { useRoles } from '../hooks/useAdmin';
import { adminLabels } from '../labels';
import { rolesForLevel } from '../utils/locationUser';

const u = adminLabels.users;
const USERNAME_MIN_LENGTH = 3;
const PASSWORD_MIN_LENGTH = 8;
const required = [{ required: true, message: common.fields.required }];

type LocationUserFieldsProps = { level: LocationLevel; namePrefix?: string[] };

/** Hududga biriktiriladigan foydalanuvchi maydonlari; rol hudud darajasidan aniqlanadi (bittadan ko'p bo'lsa — tanlanadi). */
export function LocationUserFields({ level, namePrefix = [] }: LocationUserFieldsProps) {
  const { data: roles = [] } = useRoles();
  const levelRoles = rolesForLevel(roles, level);
  const form = Form.useFormInstance();
  const defaultRole = levelRoles[0]?.code;
  const roleFieldPath = [...namePrefix, 'role'].join('.');

  // Rollar ro'yxati modal ochilgandan keyin kelishi mumkin, shuning uchun boshlang'ich qiymat effektda o'rnatiladi.
  useEffect(() => {
    if (defaultRole && !form.getFieldValue([...namePrefix, 'role'])) form.setFieldValue([...namePrefix, 'role'], defaultRole);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [defaultRole, roleFieldPath, form]);

  return (
    <>
      <Form.Item name={[...namePrefix, 'username']} label={u.username} rules={[...required, { min: USERNAME_MIN_LENGTH }]}>
        <Input autoComplete="off" />
      </Form.Item>
      <Form.Item name={[...namePrefix, 'password']} label={u.password} extra={u.passwordHint} rules={[...required, { min: PASSWORD_MIN_LENGTH }]}>
        <Input.Password autoComplete="new-password" />
      </Form.Item>
      <Form.Item name={[...namePrefix, 'fullName']} label={common.fields.fullName} rules={required}>
        <Input />
      </Form.Item>
      {levelRoles.length > 1 && (
        <Form.Item name={[...namePrefix, 'role']} label={u.role} rules={required}>
          <Select options={levelRoles.map((option) => ({ value: option.code, label: option.label }))} />
        </Form.Item>
      )}
    </>
  );
}
