import { CheckOutlined, EditOutlined, PlusOutlined, SendOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, InputNumber, Modal, Select, Space, Table, Tag } from 'antd';
import type { Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { roleLabels, useCan, type Role } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { formatDate } from '@/utils/format';
import { useCompleteDeadline, useDeadlines, useProcessDeadlines, useSaveDeadline } from '../hooks/useDeadlines';
import { deadlineLabels } from '../labels';
import type { Deadline } from '../types';

type FormValues = { name: string; description?: string; deadlineDate: Dayjs; responsibleRole: Role; escalationRole: Role; remindDaysBefore?: number };

const roleOptions = Object.entries(roleLabels).map(([value, label]) => ({ value, label }));
const required = [{ required: true, message: common.fields.required }];
/** Muddat buzilsa, respublika darajasiga (SuperAdmin) xabar beriladi. */
const DEFAULT_ESCALATION_ROLE: Role = 'SUPER_ADMIN';
const f = deadlineLabels.fields;

function DeadlineModal({ deadline, open, onClose }: { deadline: Deadline | null; open: boolean; onClose: () => void }) {
  const [form] = Form.useForm<FormValues>();
  const { mutateAsync, isPending } = useSaveDeadline();
  useEffect(() => {
    if (!open) return;
    form.resetFields();
    if (deadline) form.setFieldsValue({ ...deadline, description: deadline.description ?? undefined, deadlineDate: dayjs(deadline.deadlineDate) });
    else form.setFieldsValue({ remindDaysBefore: 7, escalationRole: DEFAULT_ESCALATION_ROLE });
  }, [open, deadline, form]);

  const handleOk = async () => {
    const { deadlineDate, ...values } = await form.validateFields().catch(() => ({} as FormValues));
    if (!values.name) return;
    try {
      await mutateAsync({ id: deadline?.id, request: { ...values, deadlineDate: deadlineDate.format(API_DATE_FORMAT) } });
      notify.success(deadlineLabels.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={deadline ? deadlineLabels.editTitle : deadlineLabels.add} onOk={handleOk} onCancel={onClose} confirmLoading={isPending}
      okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical">
        <Form.Item name="name" label={f.name} rules={required}><Input /></Form.Item>
        <Form.Item name="description" label={f.description}><Input.TextArea rows={2} /></Form.Item>
        <Form.Item name="deadlineDate" label={f.date} rules={required}><DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="responsibleRole" label={f.responsible} rules={required}><Select options={roleOptions} /></Form.Item>
        <Form.Item name="escalationRole" label={f.escalation} rules={required}><Select options={roleOptions} /></Form.Item>
        <Form.Item name="remindDaysBefore" label={f.remind}><InputNumber min={0} max={60} style={{ width: '100%' }} /></Form.Item>
      </Form>
    </Modal>
  );
}

export function DeadlineTable() {
  const canManage = useCan('deadlineManage');
  const canProcess = useCan('systemConfig');
  const { data, isLoading, error, refetch } = useDeadlines();
  const complete = useCompleteDeadline();
  const process = useProcessDeadlines();
  const [editing, setEditing] = useState<Deadline | null>(null);
  const [isOpen, setOpen] = useState(false);
  const open = (d: Deadline | null) => { setEditing(d); setOpen(true); };
  const c = deadlineLabels.columns;

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canManage && (
        <Space wrap>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => open(null)}>{deadlineLabels.add}</Button>
          {canProcess && (
            <Button icon={<SendOutlined />} loading={process.isPending}
              onClick={async () => notify.success(deadlineLabels.processed(await process.mutateAsync()))}>{deadlineLabels.process}</Button>
          )}
        </Space>
      )}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(items) => (
          <Table<Deadline> rowKey="id" dataSource={items} pagination={false} scroll={{ x: 'max-content' }}
            columns={[
              { title: c.name, dataIndex: 'name' },
              { title: c.date, dataIndex: 'deadlineDate', render: formatDate },
              { title: c.left, render: (_: unknown, d) => (d.status === 'DONE' ? '—' : <Tag color={d.overdue ? 'red' : d.daysLeft <= d.remindDaysBefore ? 'orange' : 'default'}>{deadlineLabels.days(d.daysLeft)}</Tag>) },
              { title: c.responsible, dataIndex: 'responsibleRole', render: (r: Role) => roleLabels[r] },
              { title: c.escalation, dataIndex: 'escalationRole', render: (r: Role) => roleLabels[r] },
              { title: c.status, dataIndex: 'status', render: (s: Deadline['status']) => <Tag color={s === 'DONE' ? 'green' : 'blue'}>{s === 'DONE' ? deadlineLabels.done : 'Ochiq'}</Tag> },
              ...(canManage ? [{ title: '', render: (_: unknown, d: Deadline) => (
                <Space>
                  <Button type="text" icon={<EditOutlined />} onClick={() => open(d)} aria-label={common.actions.edit} />
                  {d.status === 'OPEN' && <Button type="text" icon={<CheckOutlined />} onClick={() => complete.mutate(d.id)} aria-label={deadlineLabels.markDone} />}
                </Space>) }] : []),
            ]} />
        )}
      </QueryBoundary>
      <DeadlineModal deadline={editing} open={isOpen} onClose={() => setOpen(false)} />
    </Space>
  );
}
