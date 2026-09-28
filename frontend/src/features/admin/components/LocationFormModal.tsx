import { Form, Input, Modal, Switch } from 'antd';
import { useEffect, useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { adminLabels } from '../labels';
import { useCreateUser, useRoles, useSaveLocation } from '../hooks/useAdmin';
import type { Location, LocationInput } from '../types';
import { toCreateUserRequest, type LocationUserValues } from '../utils/locationUser';
import { LocationUserFields } from './LocationUserFields';

export type LocationFormTarget =
  | { mode: 'edit'; location: Location }
  | { mode: 'create'; level: 'DISTRICT' | 'UNIT'; parent: Location };

type LocationFormModalProps = { target: LocationFormTarget | null; onClose: () => void };

const labels = adminLabels.locations;
const CODE_MAX_LENGTH = 60;

type FormValues = LocationInput & { user?: LocationUserValues };

export function LocationFormModal({ target, onClose }: LocationFormModalProps) {
  const [form] = Form.useForm<FormValues>();
  const editedId = target?.mode === 'edit' ? target.location.id : undefined;
  const { mutateAsync, isPending } = useSaveLocation(editedId);
  const { mutateAsync: createUser, isPending: isCreatingUser } = useCreateUser();
  const { data: roles = [] } = useRoles();
  const [withUser, setWithUser] = useState(false);

  useEffect(() => {
    if (!target) return;
    form.resetFields();
    setWithUser(false);
    if (target.mode === 'edit') form.setFieldsValue({ name: target.location.name, code: target.location.code ?? undefined });
  }, [target, form]);

  const handleOk = async () => {
    if (!target) return;
    const { user, ...values } = await form.validateFields();
    try {
      if (target.mode === 'edit') {
        await mutateAsync(values);
      } else {
        const created = await mutateAsync({ ...values, level: target.level, parentId: target.parent.id });
        notify.success(common.states.saved);
        if (withUser && user) await addUser(user, target.level, created.id);
      }
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  /** Hudud allaqachon yaratilgan: foydalanuvchi qo'shilmasa, hudud saqlanib qoladi va xabar beriladi. */
  const addUser = async (user: LocationUserValues, level: 'DISTRICT' | 'UNIT', locationId: number) => {
    const request = toCreateUserRequest(user, level, locationId, roles);
    if (!request) throw new Error(labels.noRole);
    try {
      await createUser(request);
      notify.success(labels.userCreated);
    } catch (error) {
      notify.error(labels.userFailed(getErrorMessage(error)));
    }
  };

  const title = target?.mode === 'edit' ? labels.edit : target?.level === 'UNIT' ? labels.createUnit : labels.createDistrict;

  return (
    <Modal
      open={target !== null}
      title={title}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={isPending || isCreatingUser}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item name="name" label={labels.name} rules={[{ required: true, whitespace: true, message: common.fields.required }]}>
          <Input autoFocus />
        </Form.Item>
        <Form.Item name="code" label={labels.code}>
          <Input maxLength={CODE_MAX_LENGTH} />
        </Form.Item>
        {target?.mode === 'create' && (
          <>
            <Form.Item label={labels.withUser}>
              <Switch checked={withUser} onChange={setWithUser} />
            </Form.Item>
            {withUser && <LocationUserFields level={target.level} namePrefix={['user']} />}
          </>
        )}
      </Form>
    </Modal>
  );
}
