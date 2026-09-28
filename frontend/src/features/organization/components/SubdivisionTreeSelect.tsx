import { TreeSelect } from 'antd';
import { useMemo } from 'react';
import { useSubdivisions } from '../hooks/useSubdivisions';
import type { SubdivisionNode } from '../types';

type TreeOption = { value: number; title: string; children: TreeOption[] };

const toOptions = (nodes: SubdivisionNode[]): TreeOption[] =>
  nodes.map((node) => ({ value: node.id, title: node.name, children: toOptions(node.children) }));

type SubdivisionTreeSelectProps = {
  unitId: number | undefined;
  value?: number;
  onChange?: (value: number | undefined) => void;
  allowClear?: boolean;
  placeholder?: string;
  style?: React.CSSProperties;
};

/** Tanlangan harbiy qismning ierarxik bo'linmalari daraxti. */
export function SubdivisionTreeSelect({ unitId, value, onChange, allowClear = true, placeholder, style }: SubdivisionTreeSelectProps) {
  const { data = [], isLoading } = useSubdivisions(unitId);
  const options = useMemo(() => toOptions(data), [data]);
  return (
    <TreeSelect
      treeDefaultExpandAll
      showSearch
      treeNodeFilterProp="title"
      loading={isLoading}
      disabled={unitId === undefined}
      value={value}
      onChange={onChange}
      allowClear={allowClear}
      placeholder={placeholder ?? "Bo'linma"}
      treeData={options}
      style={{ minWidth: 220, ...style }}
    />
  );
}
