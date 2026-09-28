import { Form, Modal } from 'antd';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useCreateUser, useRoles } from '../hooks/useAdmin';
import { adminLabels } from '../labels';
import type { Location } from '../types';
import { toCreateUserRequest, type LocationUserValues } from '../utils/locationUser';
import { LocationUserFields } from './LocationUserFields';

type LocationUserModalProps = { location: Location | null; onClose: () => void };

const labels = adminLabels.locations;

/** Mavjud hududga foydalanuvchi qo'shish (jadval qatoridagi tugma). */
export function LocationUserModal({ location, onClose }: LocationUserModalProps) {
  const [form] = Form.useForm<LocationUserValues>();
  const { data: roles = [] } = useRoles();
  const { mutateAsync, isPending } = useCreateUser();

  const handleOk = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values || !location) return;
    const request = toCreateUserRequest(values, location.level, location.id, roles);
    if (!request) {
      notify.error(labels.noRole);
      return;
    }
    try {
      await mutateAsync(request);
      notify.success(labels.userCreated);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal
      open={location !== null}
      title={location ? labels.createUserFor(location.name) : ''}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={isPending}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        {location && <LocationUserFields level={location.level} />}
      </Form>
    </Modal>
  );
}
