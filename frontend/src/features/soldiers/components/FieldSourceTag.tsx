import { Tag } from 'antd';
import { soldierLabels } from '../labels';
import type { FieldSource } from '../types';

const TAG_COLORS: Record<FieldSource, string> = { INTEGRATION: 'blue', MANUAL: 'default' };

export function FieldSourceTag({ source }: { source: FieldSource | undefined }) {
  if (!source) return null;
  return <Tag color={TAG_COLORS[source]} style={{ marginInlineStart: 8 }}>{soldierLabels.sourceTag[source]}</Tag>;
}
