import type { Dayjs } from 'dayjs';
import { DatePicker, Form, Input, Modal, Select } from 'antd';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { useAssignLeader, useLeaderOptions } from '../hooks/useGroups';
import { groupLabels } from '../labels';
import type { Group } from '../types';
import { notify } from '@/lib/notify';

type FormValues = { userId: number; orderNo: string; orderDate: Dayjs };

const required = [{ required: true, message: common.fields.required }];

export function LeaderAssignModal({ group, open, onClose }: { group: Group; open: boolean; onClose: () => void }) {
  const [form] = Form.useForm<FormValues>();
  const { data: options = [] } = useLeaderOptions(group.militaryUnitId, open);
  const { mutateAsync, isPending } = useAssignLeader(group.id);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await mutateAsync({ ...values, orderDate: values.orderDate.format(API_DATE_FORMAT) });
      notify.success(groupLabels.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal
      open={open}
      title={groupLabels.assignLeader}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={isPending}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" initialValues={{ userId: group.leader?.id, orderNo: group.leaderOrderNo ?? undefined }}>
        <Form.Item name="userId" label={groupLabels.leaderUser} rules={required}>
          <Select options={options.map((option) => ({ value: option.id, label: option.fullName }))} />
        </Form.Item>
        <Form.Item name="orderNo" label={groupLabels.fields.orderNo} rules={required}><Input /></Form.Item>
        <Form.Item name="orderDate" label={groupLabels.fields.orderDate} rules={required}>
          <DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
