import { Form, Input, Modal } from 'antd';
import { MilitaryUnitSelect, SubdivisionTreeSelect } from '@/features/organization';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useTransfer } from '../hooks/useSoldiers';
import { soldierLabels } from '../labels';

type FormValues = { militaryUnitId: number; subdivisionId?: number; reason: string };

type TransferModalProps = { soldierId: number; open: boolean; onClose: () => void; onDone: () => void };

const required = [{ required: true, message: common.fields.required }];

export function TransferModal({ soldierId, open, onClose, onDone }: TransferModalProps) {
  const [form] = Form.useForm<FormValues>();
  const unitId = Form.useWatch('militaryUnitId', form) as number | undefined;
  const { mutateAsync, isPending } = useTransfer(soldierId);
  const t = soldierLabels.transfer;

  const handleOk = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values) return;
    try {
      await mutateAsync(values);
      notify.success(t.done);
      form.resetFields();
      onDone();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={t.title} onOk={handleOk} onCancel={onClose} confirmLoading={isPending}
      okText={common.actions.confirm} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical">
        <Form.Item name="militaryUnitId" label={t.unit} rules={required}>
          <MilitaryUnitSelect style={{ width: '100%' }} onChange={() => form.setFieldValue('subdivisionId', undefined)} />
        </Form.Item>
        <Form.Item name="subdivisionId" label={soldierLabels.fields.subdivision}>
          <SubdivisionTreeSelect unitId={unitId} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="reason" label={t.reason} rules={required}><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  );
}
