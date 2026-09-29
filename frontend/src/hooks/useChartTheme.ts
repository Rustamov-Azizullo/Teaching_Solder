import { theme } from 'antd';
import type { CSSProperties } from 'react';

export type ChartTheme = {
  text: string;
  grid: string;
  tick: { fill: string; fontSize: number };
  tooltip: CSSProperties;
  cursor: { fill: string };
};

const TICK_FONT_SIZE = 11;

/** Diagramma o'qlari, to'r va tooltip ranglarini joriy (kun/tun) mavzudan oladi. */
export function useChartTheme(): ChartTheme {
  const { token } = theme.useToken();
  return {
    text: token.colorTextSecondary,
    grid: token.colorBorderSecondary,
    tick: { fill: token.colorTextSecondary, fontSize: TICK_FONT_SIZE },
    tooltip: {
      background: token.colorBgElevated,
      border: `1px solid ${token.colorBorderSecondary}`,
      borderRadius: token.borderRadius,
      color: token.colorText,
      fontSize: TICK_FONT_SIZE + 1,
    },
    cursor: { fill: 'rgba(128, 128, 128, 0.12)' },
  };
}
