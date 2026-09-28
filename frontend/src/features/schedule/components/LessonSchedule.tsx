import type { Dayjs } from 'dayjs';
import { CalendarOutlined, PlusOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Alert, Button, DatePicker, Input, Modal, Space, Table, Tag } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { formatDate, formatTime } from '@/utils/format';
import { useCancelLesson, useGenerateLessons, useGroupLessons } from '../hooks/useLessons';
import { lessonKindLabels, lessonStatusLabels, scheduleLabels } from '../labels';
import type { Lesson, LessonStatus } from '../types';
import { LessonFormModal } from './LessonFormModal';
import { notify } from '@/lib/notify';

const STATUS_COLORS: Record<LessonStatus, string> = { PLANNED: 'blue', HELD: 'green', CANCELLED: 'default' };

type LessonScheduleProps = { groupId: number; groupStart: string; groupEnd: string; canEdit: boolean };

export function LessonSchedule({ groupId, groupStart, groupEnd, canEdit }: LessonScheduleProps) {
  const [range, setRange] = useState<[Dayjs, Dayjs]>([dayjs().startOf('month'), dayjs().endOf('month')]);
  const [isModalOpen, setModalOpen] = useState(false);
  const from = range[0].format(API_DATE_FORMAT);
  const to = range[1].format(API_DATE_FORMAT);
  const { data, isLoading, error, refetch } = useGroupLessons(groupId, from, to);
  const generate = useGenerateLessons(groupId);
  const cancel = useCancelLesson();
  const [cancelReason, setCancelReason] = useState('');
  const [lessonToCancel, setLessonToCancel] = useState<Lesson | null>(null);

  const handleGenerate = async () => {
    try {
      const created = await generate.mutateAsync({ from, to, kind: 'THEORY' });
      notify.success(scheduleLabels.generated(created.length));
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  const handleCancel = async () => {
    if (!lessonToCancel) return;
    try {
      await cancel.mutateAsync({ lessonId: lessonToCancel.id, reason: cancelReason });
      notify.success(scheduleLabels.cancelled);
      setLessonToCancel(null);
      setCancelReason('');
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Alert type="info" showIcon icon={<CalendarOutlined />} message={scheduleLabels.standardTime}
        description={`${formatDate(groupStart)} — ${formatDate(groupEnd)}`} />
      <Space wrap>
        <DatePicker.RangePicker
          value={range}
          format={DISPLAY_DATE_FORMAT}
          allowClear={false}
          onChange={(value) => value?.[0] && value[1] && setRange([value[0], value[1]])}
        />
        {canEdit && (
          <>
            <Button icon={<ThunderboltOutlined />} loading={generate.isPending} onClick={handleGenerate} title={scheduleLabels.generateHint}>
              {scheduleLabels.generate}
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>{scheduleLabels.add}</Button>
          </>
        )}
      </Space>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(lessons) => (
          <Table<Lesson>
            rowKey="id"
            dataSource={lessons}
            pagination={{ pageSize: 15, hideOnSinglePage: true }}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: scheduleLabels.columns.date, dataIndex: 'lessonDate', render: formatDate },
              { title: scheduleLabels.columns.time, render: (_: unknown, l) => `${formatTime(l.startTime)}–${formatTime(l.endTime)}` },
              { title: scheduleLabels.columns.hours, dataIndex: 'academicHours' },
              { title: scheduleLabels.columns.kind, dataIndex: 'kind', render: (kind: Lesson['kind']) => lessonKindLabels[kind] },
              { title: scheduleLabels.columns.topic, dataIndex: 'topic' },
              {
                title: scheduleLabels.columns.status,
                dataIndex: 'status',
                render: (status: LessonStatus, l) => (
                  <Tag color={STATUS_COLORS[status]} title={l.changeReason ?? undefined}>{lessonStatusLabels[status]}</Tag>
                ),
              },
              {
                title: '',
                render: (_: unknown, l) => (
                  <Space>
                    {canEdit && l.status === 'PLANNED' && (
                      <Button size="small" danger type="link" onClick={() => setLessonToCancel(l)}>{scheduleLabels.cancel}</Button>
                    )}
                  </Space>
                ),
              },
            ]}
          />
        )}
      </QueryBoundary>
      <LessonFormModal groupId={groupId} open={isModalOpen} onClose={() => setModalOpen(false)} />
      <Modal
        open={lessonToCancel !== null}
        title={scheduleLabels.cancel}
        onOk={handleCancel}
        okButtonProps={{ disabled: !cancelReason.trim(), danger: true, loading: cancel.isPending }}
        okText={common.actions.confirm}
        cancelText={common.actions.cancel}
        onCancel={() => setLessonToCancel(null)}
      >
        <Input.TextArea rows={3} placeholder={scheduleLabels.cancelReason} value={cancelReason} onChange={(e) => setCancelReason(e.target.value)} />
      </Modal>
    </Space>
  );
}
