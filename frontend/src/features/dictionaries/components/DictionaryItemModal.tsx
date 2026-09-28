import { Form, Input, InputNumber, Modal, Switch } from 'antd';
import { useEffect } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { dictionaryLabels } from '../labels';
import { useSaveDictionaryItem } from '../hooks/useDictionary';
import type { DictionaryItem, DictionaryItemInput, DictionaryType } from '../types';
import { notify } from '@/lib/notify';

type DictionaryItemModalProps = {
  type: DictionaryType;
  item: DictionaryItem | null;
  open: boolean;
  onClose: () => void;
};

export function DictionaryItemModal({ type, item, open, onClose }: DictionaryItemModalProps) {
  const [form] = Form.useForm<DictionaryItemInput>();
  const { mutateAsync, isPending } = useSaveDictionaryItem(type);
  const supportsHours = (dictionaryLabels.hoursSupported as readonly string[]).includes(type);

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue(item ? { ...item, description: item.description ?? undefined } : { active: true });
  }, [open, item, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await mutateAsync({ id: item?.id, input: values });
      notify.success(common.states.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal
      open={open}
      title={item ? dictionaryLabels.editTitle : dictionaryLabels.createTitle}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={isPending}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item name="name" label={common.fields.name} rules={[{ required: true, message: common.fields.required }]}>
          <Input />
        </Form.Item>
        <Form.Item
          name="code"
          label={common.fields.code}
          extra={dictionaryLabels.codeHint}
          rules={[{ required: true, message: common.fields.required }]}
        >
          <Input disabled={item !== null} />
        </Form.Item>
        {supportsHours && (
          <Form.Item name="hours" label={dictionaryLabels.hours}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        )}
        <Form.Item name="description" label={common.fields.description}>
          <Input.TextArea rows={2} />
        </Form.Item>
        <Form.Item name="active" label={common.fields.active} valuePropName="checked">
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  );
}
