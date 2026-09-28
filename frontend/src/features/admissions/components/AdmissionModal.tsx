import { Form, Input, InputNumber, Modal, Select, Switch } from 'antd';
import { useEffect } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useUpdateAdmission } from '../hooks/useAdmissions';
import { admissionLabels, onlineStatusLabels, studyFormLabels } from '../labels';
import type { AdmissionRow, AdmissionUpdate } from '../types';

const f = admissionLabels.fields;
const toOptions = (labels: Record<string, string>) => Object.entries(labels).map(([value, label]) => ({ value, label }));

export function AdmissionModal({ row, onClose }: { row: AdmissionRow | null; onClose: () => void }) {
  const [form] = Form.useForm<AdmissionUpdate>();
  const admitted = Form.useWatch('admitted', form);
  const { mutateAsync, isPending } = useUpdateAdmission();

  useEffect(() => {
    if (row) form.setFieldsValue(row);
  }, [row, form]);

  const handleOk = async () => {
    const update = await form.validateFields().catch(() => null);
    if (!update || !row) return;
    try {
      await mutateAsync({ soldierId: row.soldierId, update });
      notify.success(admissionLabels.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={row !== null} title={`${admissionLabels.editTitle}: ${row?.fullName ?? ''}`} onOk={handleOk} onCancel={onClose}
      confirmLoading={isPending} okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical">
        <Form.Item name="bmbaRegistered" label={f.bmba} valuePropName="checked"><Switch /></Form.Item>
        <Form.Item name="benefitsUploaded" label={f.benefits} valuePropName="checked"><Switch /></Form.Item>
        <Form.Item name="testParticipated" label={f.test} valuePropName="checked"><Switch /></Form.Item>
        <Form.Item name="testScore" label={f.score}><InputNumber min={0} max={200} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="admitted" label={f.admitted} valuePropName="checked"><Switch /></Form.Item>
        {admitted && (
          <>
            <Form.Item name="university" label={f.university} rules={[{ required: true, message: common.fields.required }]}><Input /></Form.Item>
            <Form.Item name="studyDirection" label={f.direction}><Input /></Form.Item>
            <Form.Item name="studyForm" label={f.form}><Select allowClear options={toOptions(studyFormLabels)} /></Form.Item>
          </>
        )}
        <Form.Item name="onlineStatus" label={f.online}><Select options={toOptions(onlineStatusLabels)} /></Form.Item>
      </Form>
    </Modal>
  );
}
