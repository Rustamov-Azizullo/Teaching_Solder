import { DeleteOutlined, EditOutlined, PaperClipOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { AttachmentPanel } from '@/features/attachments';
import { useCan, useAuth } from '@/features/auth';
import { useDictionary } from '@/features/dictionaries';
import { MilitaryUnitSelect } from '@/features/organization';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useFacilities, useRemoveFacility, useSaveFacility } from '../hooks/useFacilities';
import { facilityConditionLabels, facilityKindLabels, facilityLabels } from '../labels';
import type { Facility, FacilityCondition, FacilityKind } from '../types';

type FormValues = {
  militaryUnitId: number; name: string; kind: FacilityKind; capacity?: number; condition: FacilityCondition;
  equipment?: string; shortages?: string; surveyDate?: Dayjs | null; suitableForIds?: number[];
};

const required = [{ required: true, message: common.fields.required }];
const toOptions = (labels: Record<string, string>) => Object.entries(labels).map(([value, label]) => ({ value, label }));

function FacilityModal({ facility, open, onClose }: { facility: Facility | null; open: boolean; onClose: () => void }) {
  const { user } = useAuth();
  const [form] = Form.useForm<FormValues>();
  const { data: professions = [] } = useDictionary('PROFESSION');
  const { data: subjects = [] } = useDictionary('SUBJECT');
  const { mutateAsync, isPending } = useSaveFacility(facility?.id);
  const f = facilityLabels.fields;

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue(facility
      ? { ...facility, capacity: facility.capacity ?? undefined, equipment: facility.equipment ?? undefined, shortages: facility.shortages ?? undefined,
          surveyDate: facility.surveyDate ? dayjs(facility.surveyDate) : null, suitableForIds: facility.suitableFor.map((i) => i.id) }
      : { militaryUnitId: user?.militaryUnitId ?? undefined, kind: 'CLASSROOM', condition: 'GOOD' });
  }, [open, facility, form, user]);

  const handleOk = async () => {
    const { surveyDate, suitableForIds, ...values } = await form.validateFields().catch(() => ({} as FormValues));
    if (!values.name) return;
    try {
      await mutateAsync({ ...values, surveyDate: surveyDate?.format(API_DATE_FORMAT), suitableForIds: suitableForIds ?? [] });
      notify.success(facilityLabels.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={facility ? facilityLabels.editTitle : facilityLabels.add} onOk={handleOk} onCancel={onClose} confirmLoading={isPending}
      okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical">
        <Form.Item name="militaryUnitId" label={common.fields.unit} rules={required}><MilitaryUnitSelect style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="name" label={common.fields.name} rules={required}><Input /></Form.Item>
        <Form.Item name="kind" label={facilityLabels.columns.kind}><Select options={toOptions(facilityKindLabels)} /></Form.Item>
        <Form.Item name="capacity" label={f.capacity}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="condition" label={facilityLabels.columns.condition}><Select options={toOptions(facilityConditionLabels)} /></Form.Item>
        <Form.Item name="equipment" label={f.equipment}><Input.TextArea rows={2} /></Form.Item>
        <Form.Item name="shortages" label={f.shortages}><Input.TextArea rows={2} /></Form.Item>
        <Form.Item name="surveyDate" label={f.surveyDate}><DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="suitableForIds" label={f.suitable}>
          <Select mode="multiple" optionFilterProp="label"
            options={[...professions, ...subjects].map((i) => ({ value: i.id, label: i.name }))} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

export function FacilityManager() {
  const canWrite = useCan('groupWrite');
  const { data, isLoading, error, refetch } = useFacilities();
  const remove = useRemoveFacility();
  const [editing, setEditing] = useState<Facility | null>(null);
  const [isOpen, setOpen] = useState(false);
  const [attaching, setAttaching] = useState<Facility | null>(null);

  const open = (facility: Facility | null) => { setEditing(facility); setOpen(true); };
  const handleDelete = async (id: number) => {
    try { await remove.mutateAsync(id); notify.success(facilityLabels.deleted); } catch (err) { notify.error(getErrorMessage(err)); }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canWrite && <Button type="primary" icon={<PlusOutlined />} onClick={() => open(null)}>{facilityLabels.add}</Button>}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(items) => (
          <Table<Facility> rowKey="id" dataSource={items} pagination={false} scroll={{ x: 'max-content' }}
            columns={[
              { title: facilityLabels.columns.unit, dataIndex: 'militaryUnitName' },
              { title: facilityLabels.columns.name, dataIndex: 'name' },
              { title: facilityLabels.columns.kind, dataIndex: 'kind', render: (k: FacilityKind) => facilityKindLabels[k] },
              { title: facilityLabels.columns.capacity, dataIndex: 'capacity' },
              { title: facilityLabels.columns.condition, dataIndex: 'condition',
                render: (c: FacilityCondition) => <Tag color={c === 'GOOD' ? 'green' : c === 'SATISFACTORY' ? 'blue' : 'red'}>{facilityConditionLabels[c]}</Tag> },
              { title: facilityLabels.columns.shortages, dataIndex: 'shortages', render: (v: string | null) => v ?? '—' },
              { title: '', render: (_: unknown, row) => (
                <Space>
                  <Button type="text" icon={<PaperClipOutlined />} onClick={() => setAttaching(row)} aria-label={facilityLabels.act} />
                  {canWrite && <Button type="text" icon={<EditOutlined />} onClick={() => open(row)} aria-label={common.actions.edit} />}
                  {canWrite && (
                    <Popconfirm title={facilityLabels.confirmDelete} onConfirm={() => handleDelete(row.id)}>
                      <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                    </Popconfirm>
                  )}
                </Space>) },
            ]}
          />
        )}
      </QueryBoundary>
      <FacilityModal facility={editing} open={isOpen} onClose={() => setOpen(false)} />
      <Modal open={attaching !== null} title={facilityLabels.act} footer={null} onCancel={() => setAttaching(null)} destroyOnHidden>
        {attaching && <AttachmentPanel ownerType="FACILITY" ownerId={attaching.id} title={attaching.name} />}
      </Modal>
    </Space>
  );
}
