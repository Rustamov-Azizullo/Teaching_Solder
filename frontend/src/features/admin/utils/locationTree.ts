import type { Location } from '../types';

export type LocationTreeNode = Location & { children?: LocationTreeNode[] };

/** Tekis ro'yxatni `parentId` bo'yicha daraxtga aylantiradi; ota topilmasa (vakolat doirasi) tugun ildiz bo'ladi. */
export function buildLocationTree(locations: Location[]): LocationTreeNode[] {
  const nodes = new Map<number, LocationTreeNode>(locations.map((location) => [location.id, { ...location }]));
  const roots: LocationTreeNode[] = [];
  for (const node of nodes.values()) {
    const parent = node.parentId === null ? undefined : nodes.get(node.parentId);
    if (!parent) {
      roots.push(node);
      continue;
    }
    parent.children = [...(parent.children ?? []), node].sort((a, b) => a.name.localeCompare(b.name));
  }
  return roots;
}
