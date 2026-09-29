import type { SubdivisionNode } from '../types';

/** Jadval qatori: bo'sh `children` antd da yolg'on "ochiladigan" belgi chiqarmasligi uchun `undefined` bo'ladi. */
export type SubdivisionRow = Omit<SubdivisionNode, 'children'> & { number: string; children?: SubdivisionRow[] };

/** Daraxtni ierarxik tartib raqamlari (1, 1.1, 1.1.1 ...) bilan jadval qatorlariga aylantiradi. */
export function toRows(nodes: SubdivisionNode[], prefix = ''): SubdivisionRow[] {
  return nodes.map((node, index) => {
    const number = `${prefix}${index + 1}`;
    const children = toRows(node.children, `${number}.`);
    return { ...node, number, children: children.length > 0 ? children : undefined };
  });
}

/** Ildizdan berilgan bo'linmagacha nomlar (batalon → rota → vzvod); topilmasa — bo'sh ro'yxat. */
export function pathNames(nodes: SubdivisionNode[], id: number | undefined): string[] {
  for (const node of nodes) {
    if (node.id === id) return [node.name];
    const below = pathNames(node.children, id);
    if (below.length > 0) return [node.name, ...below];
  }
  return [];
}

export function countNodes(nodes: SubdivisionNode[]): number {
  return nodes.reduce((sum, node) => sum + 1 + countNodes(node.children), 0);
}

export function countSoldiers(nodes: SubdivisionNode[]): number {
  return nodes.reduce((sum, node) => sum + node.soldierCount + countSoldiers(node.children), 0);
}
