import { CheckCircleFilled, ClockCircleOutlined } from '@ant-design/icons';
import { Card, Empty, List, Tag } from 'antd';
import { Link } from 'react-router-dom';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { lessonKindLabels, useLessonsByDate, type Lesson } from '@/features/schedule';
import { API_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { formatTime } from '@/utils/format';
import { attendanceLabels } from '../labels';

function LessonCard({ lesson }: { lesson: Lesson }) {
  const isCancelled = lesson.status === 'CANCELLED';
  return (
    <List.Item>
      <Link to={`/attendance/${lesson.id}`} style={{ width: '100%' }} aria-disabled={isCancelled}>
        <Card hoverable size="small">
          <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, flexWrap: 'wrap' }}>
            <div>
              <div style={{ fontWeight: 600 }}>{lesson.groupName}</div>
              <span style={{ opacity: 0.7 }}>
                <ClockCircleOutlined /> {formatTime(lesson.startTime)}–{formatTime(lesson.endTime)} · {lessonKindLabels[lesson.kind]}
                {lesson.topic ? ` · ${lesson.topic}` : ''}
              </span>
            </div>
            {isCancelled ? (
              <Tag>Bekor qilingan</Tag>
            ) : lesson.attendanceRecorded ? (
              <Tag color="green" icon={<CheckCircleFilled />}>{attendanceLabels.recorded}</Tag>
            ) : (
              <Tag color="orange">{attendanceLabels.notRecorded}</Tag>
            )}
          </div>
        </Card>
      </Link>
    </List.Item>
  );
}

export function TodayLessons() {
  const today = dayjs().format(API_DATE_FORMAT);
  const { data, isLoading, error, refetch } = useLessonsByDate(today);

  return (
    <>
      <PageHeader title={attendanceLabels.todayTitle} subtitle={attendanceLabels.todaySubtitle} />
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(lessons) =>
          lessons.length === 0 ? (
            <Empty description={attendanceLabels.todayEmpty} />
          ) : (
            <List grid={{ gutter: 12, xs: 1, sm: 1, md: 2, lg: 2, xl: 3 }} dataSource={lessons} renderItem={(lesson) => <LessonCard lesson={lesson} />} />
          )
        }
      </QueryBoundary>
    </>
  );
}
