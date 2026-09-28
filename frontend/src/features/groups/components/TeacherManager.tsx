import { EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, Modal, Select, Space, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import type { Dayjs } from 'dayjs';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { MilitaryUnitSelect } from '@/features/organization';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { formatDate } from '@/utils/format';
import { useCreateInstitution, useInstitutions, useSaveTeacher, useTeachers } from '../hooks/useGroups';
import { groupLabels, institutionTypeLabels } from '../labels';
import type { InstitutionType, Teacher } from '../types';
import { notify } from '@/lib/notify';

type TeacherFormValues = {
  fullName: string; specialty: string; institutionId: number; militaryUnitId: number;
  accessOrderNo?: string; accessValidUntil?: Dayjs | null;
};

const required = [{ required: true, message: common.fields.required }];
const t = groupLabels.teachers;

function InstitutionModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [form] = Form.useForm<{ type: InstitutionType; name: string }>();
  const { mutateAsync, isPending } = useCreateInstitution();
  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await mutateAsync(values);
      notify.success(common.states.saved);
      form.resetFields();
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };
  return (
    <Modal open={open} title={t.addInstitution} onOk={handleOk} onCancel={onClose} confirmLoading={isPending}
      okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical" initialValues={{ type: 'TECHNICAL_SCHOOL' }}>
        <Form.Item name="type" label={t.institutionType}>
          <Select options={Object.entries(institutionTypeLabels).map(([value, label]) => ({ value, label }))} />
        </Form.Item>
        <Form.Item name="name" label={t.institutionName} rules={required}><Input /></Form.Item>
      </Form>
    </Modal>
  );
}

function TeacherModal({ teacher, open, onClose }: { teacher: Teacher | null; open: boolean; onClose: () => void }) {
  const [form] = Form.useForm<TeacherFormValues>();
  const { data: institutions = [] } = useInstitutions();
  const { mutateAsync, isPending } = useSaveTeacher(teacher?.id);

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    if (teacher) {
      form.setFieldsValue({ ...teacher, accessValidUntil: teacher.accessValidUntil ? dayjs(teacher.accessValidUntil) : null, accessOrderNo: teacher.accessOrderNo ?? undefined });
    }
  }, [open, teacher, form]);

  const handleOk = async () => {
    const { accessValidUntil, ...values } = await form.validateFields();
    try {
      await mutateAsync({ ...values, accessValidUntil: accessValidUntil?.format(API_DATE_FORMAT) });
      notify.success(common.states.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={t.add} onOk={handleOk} onCancel={onClose} confirmLoading={isPending}
      okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical">
        <Form.Item name="fullName" label={common.fields.fullName} rules={required}><Input /></Form.Item>
        <Form.Item name="specialty" label={t.specialty} rules={required}><Input /></Form.Item>
        <Form.Item name="institutionId" label={t.institution} rules={required}>
          <Select options={institutions.map((item) => ({ value: item.id, label: item.name }))} />
        </Form.Item>
        <Form.Item name="militaryUnitId" label={common.fields.unit} rules={required}>
          <MilitaryUnitSelect style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="accessOrderNo" label={t.accessOrder}><Input /></Form.Item>
        <Form.Item name="accessValidUntil" label={t.accessUntil}>
          <DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

export function TeacherManager() {
  const canEdit = useCan('groupWrite');
  const { data, isLoading, error, refetch } = useTeachers();
  const [editing, setEditing] = useState<Teacher | null>(null);
  const [isTeacherOpen, setTeacherOpen] = useState(false);
  const [isInstitutionOpen, setInstitutionOpen] = useState(false);

  const openTeacher = (teacher: Teacher | null) => {
    setEditing(teacher);
    setTeacherOpen(true);
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canEdit && (
        <Space wrap>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openTeacher(null)}>{t.add}</Button>
          <Button onClick={() => setInstitutionOpen(true)}>{t.addInstitution}</Button>
        </Space>
      )}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(teachers) => (
          <Table<Teacher>
            rowKey="id"
            dataSource={teachers}
            pagination={false}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: common.fields.fullName, dataIndex: 'fullName' },
              { title: t.specialty, dataIndex: 'specialty' },
              { title: t.institution, dataIndex: 'institutionName' },
              { title: t.accessOrder, dataIndex: 'accessOrderNo' },
              {
                title: t.accessUntil,
                dataIndex: 'accessValidUntil',
                render: (value: string | null, row) => (
                  <>{formatDate(value)} {row.accessExpiringSoon && <Tag color="orange">{t.expiring}</Tag>}</>
                ),
              },
              ...(canEdit
                ? [{ title: '', render: (_: unknown, row: Teacher) => <Button type="text" icon={<EditOutlined />} onClick={() => openTeacher(row)} aria-label={common.actions.edit} /> }]
                : []),
            ]}
          />
        )}
      </QueryBoundary>
      <TeacherModal teacher={editing} open={isTeacherOpen} onClose={() => setTeacherOpen(false)} />
      <InstitutionModal open={isInstitutionOpen} onClose={() => setInstitutionOpen(false)} />
    </Space>
  );
}
