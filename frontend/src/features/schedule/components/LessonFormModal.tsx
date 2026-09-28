import type { Dayjs } from 'dayjs';
import { DatePicker, Form, Input, Modal, Select } from 'antd';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { useCreateLesson } from '../hooks/useLessons';
import { lessonKindLabels, scheduleLabels } from '../labels';
import type { LessonKind } from '../types';
import { notify } from '@/lib/notify';

type FormValues = { lessonDate: Dayjs; topic?: string; kind: LessonKind };

type LessonFormModalProps = { groupId: number; open: boolean; onClose: () => void };

export function LessonFormModal({ groupId, open, onClose }: LessonFormModalProps) {
  const [form] = Form.useForm<FormValues>();
  const { mutateAsync, isPending } = useCreateLesson(groupId);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await mutateAsync({ ...values, lessonDate: values.lessonDate.format(API_DATE_FORMAT) });
      notify.success(common.states.saved);
      form.resetFields();
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal
      open={open}
      title={scheduleLabels.add}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={isPending}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" initialValues={{ kind: 'THEORY' }}>
        <Form.Item name="lessonDate" label={common.fields.date} rules={[{ required: true, message: common.fields.required }]}>
          <DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="kind" label={scheduleLabels.kind}>
          <Select options={Object.entries(lessonKindLabels).map(([value, label]) => ({ value, label }))} />
        </Form.Item>
        <Form.Item name="topic" label={scheduleLabels.topic}><Input /></Form.Item>
      </Form>
    </Modal>
  );
}
