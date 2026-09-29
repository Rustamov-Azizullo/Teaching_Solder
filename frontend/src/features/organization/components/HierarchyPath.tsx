import { RightOutlined } from '@ant-design/icons';
import { Tag, Typography } from 'antd';
import { Fragment } from 'react';

/** Har daraja o'z rangida: vazirlik, okrug, qism, keyin bo'linmalar (batalon → rota → vzvod). */
const LEVEL_COLORS = ['blue', 'geekblue', 'cyan'] as const;
const SUBDIVISION_COLOR = 'default';

type HierarchyPathProps = { levels: string[]; subdivisions: string[] };

/** Tanlangan joyning to'liq yo'li: masalan «Vazirlik → Okrug → Qism → Batalon → Rota → Vzvod». */
export function HierarchyPath({ levels, subdivisions }: HierarchyPathProps) {
  const steps = [
    ...levels.map((name, index) => ({ name, color: LEVEL_COLORS[index] ?? SUBDIVISION_COLOR })),
    ...subdivisions.map((name) => ({ name, color: SUBDIVISION_COLOR })),
  ];
  const lastIndex = steps.length - 1;
  return (
    <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', rowGap: 8 }}>
      {steps.map((step, index) => (
        <Fragment key={`${index}-${step.name}`}>
          <Tag color={step.color} style={{ margin: 0, fontSize: 14, padding: '2px 10px', fontWeight: index === lastIndex ? 600 : 400 }}>
            {step.name}
          </Tag>
          {index < lastIndex && <Typography.Text type="secondary" style={{ margin: '0 8px' }}><RightOutlined /></Typography.Text>}
        </Fragment>
      ))}
    </div>
  );
}
