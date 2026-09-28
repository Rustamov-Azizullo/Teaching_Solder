---
name: react-clean-architecture
description: >
  Decide where files go and how modules depend on each other in a feature-based
  React + TypeScript codebase. USE THIS SKILL whenever creating a new file,
  component, hook, service, or feature; when moving/renaming files; when adding
  imports; or when reviewing project structure — even if the user does not say
  "architecture". If you are about to create a `.tsx`/`.ts` file or write an
  import statement, consult this skill first to avoid misplacing code or crossing
  feature boundaries.
---

# React Clean Architecture (Feature-Based)

This skill defines **where code lives** and **who may depend on whom**. Follow it
for every file you create, move, or import.

## The one rule that drives everything: colocation

Keep files close to where they are used. Something used by exactly one feature
lives inside that feature. Something used by two or more features moves up to a
shared folder. Do not "promote" code to shared folders speculatively — promote
only when a second consumer actually appears.

## Decision procedure — "where does this file go?"

Ask these questions in order and stop at the first "yes":

1. **Is it a route/screen the router points to?** → `src/pages/`.
   Pages compose features; they contain little logic of their own.

2. **Does it belong to exactly one feature (auth, cart, products, ...)?**
   → `src/features/<feature>/`, in the matching subfolder:
   - UI → `components/`
   - stateful logic / data hooks → `hooks/`
   - server calls → `api/`
   - types → `types.ts`
   Export anything other features need from `index.ts` (see "Public API" below).

3. **Is it reusable UI with no business meaning?** (Button, Modal, Input, Table)
   → `src/components/` (primitives in `src/components/ui/`).

4. **Is it a reusable hook with no feature meaning?** (useDebounce, useMediaQuery)
   → `src/hooks/`.

5. **Is it a pure function, no React, no side effects?** (formatDate, slugify)
   → `src/utils/`.

6. **Is it third-party client setup?** (axios instance, query client, dayjs config)
   → `src/lib/`.

7. **Is it a cross-cutting service or global state?**
   → `src/services/` (domain/API services) or `src/stores/` (global state).

If two answers seem to apply, prefer the **most specific / most local** one.
A thing that "could be shared someday" but is used once today goes in the feature.

## Public API of a feature (the barrel)

Each feature exposes a single entry point, `index.ts`. Other parts of the app
import **only** from there:

```ts
// features/products/index.ts
export { ProductList } from './components/ProductList';
export { useProducts } from './hooks/useProducts';
export type { Product } from './types';
// Internal files (ProductRow, productMapper, etc.) are NOT exported.
```

```ts
// ✅ allowed
import { ProductList } from '@/features/products';
// ❌ forbidden — reaching into internals
import { ProductRow } from '@/features/products/components/ProductRow';
```

Keep barrels thin. Over-barrelling (re-exporting everything) causes circular
dependencies and slow builds. Export only the feature's real surface.

## Dependency direction (must not be violated)

```
pages  ──▶ features ──▶ shared (components, hooks, utils, lib, services, stores, types)
```

- Arrows point one way only. Shared code **never** imports from `features/` or
  `pages/`. Features **never** import another feature's internals.
- If two features need the same logic, extract it to a shared folder (or a new
  small feature) — do not import feature A from feature B.
- A cycle (A → B → A) is always a design smell. Break it by moving the shared
  piece down into a shared layer.

## Naming & file conventions

- Components: `PascalCase.tsx` → `UserCard.tsx`.
- Hooks: `useSomething.ts` → `useAuth.ts`.
- Utilities/services: `camelCase.ts` → `formatDate.ts`, `authService.ts`.
- Types file per feature: `types.ts`. Shared types: `src/types/`.
- One component per file. The file name matches the exported component.

### Colocating tests and styles

Put tests and styles next to the unit they cover:

```
UserCard/
├── UserCard.tsx
├── UserCard.test.tsx
├── UserCard.module.css
└── index.ts        # optional: re-export UserCard
```

Prefer the folder-per-component form once a component grows companions
(test + styles + subcomponents). A lone component can stay a single file.

## Path alias

Configure `@/` → `src/` in `tsconfig.json` and the bundler
(`vite.config.ts` / `resolve.alias`). Always import via `@/…`; never use
`../../../`. This keeps imports stable when files move.

## Framework note

If the project uses **Next.js App Router**, `app/` is reserved for routing
(`page.tsx`, `layout.tsx`). Keep it thin and put logic in `features/`,
`components/`, `hooks/`, `lib/` exactly as above — the routing folder replaces
`src/pages/`, everything else is unchanged.

## Quick checklist before creating a file

- [ ] Ran the decision procedure; picked the most local correct folder.
- [ ] If feature-internal, it is NOT exported unless another module needs it.
- [ ] New cross-feature need → extracted to shared, not imported sideways.
- [ ] Import uses `@/` and respects dependency direction.
- [ ] Name follows the convention for its kind.
