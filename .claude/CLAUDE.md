# Project Guidelines — React + TypeScript (Clean Architecture)

You are working in a React + TypeScript codebase that follows a **feature-based
(feature-sliced) architecture**, **SOLID principles**, and established **design
patterns**. Follow the rules below in every response that reads, writes, or
reviews code in this repository.

## Non-negotiable rules

1. **Read the rules before writing code.** The `.claude/rules/` directory is the
   source of truth for how code must be written here:
   - `rules/clean-code.md` — naming, functions, comments, error handling.
   - `rules/solid-principles.md` — SOLID applied to React/TypeScript.
   - `rules/design-patterns.md` — approved patterns and when to use them.
   When a task involves authoring or refactoring code, consult the relevant rule
   file first and make your output conform to it.

2. **Respect the architecture.** File placement follows the
   `react-clean-architecture` skill. Do not put feature logic in shared folders,
   and do not import across feature boundaries except through a feature's public
   `index.ts` barrel.

3. **TypeScript is strict.** No `any` unless justified in a comment. Prefer
   explicit types on public APIs (props, hook returns, service signatures).
   Type inference is fine for local variables.

4. **Components stay small and focused.** A component that fetches data, holds
   business logic, AND renders complex UI is doing too much — split it. See the
   `react-component-patterns` skill.

## Directory map (feature-based)

```
src/
├── app/          # App bootstrap: providers, router, global config
├── pages/        # Route-level screens (compose features)
├── features/     # ⭐ Self-contained feature modules (auth, products, ...)
│   └── <feature>/
│       ├── components/   # UI private to this feature
│       ├── hooks/        # Logic private to this feature
│       ├── api/          # Server calls for this feature
│       ├── types.ts
│       └── index.ts      # PUBLIC API — the only thing others may import
├── components/   # Shared, reusable UI (components/ui = primitives)
├── hooks/        # Global reusable hooks
├── lib/          # Third-party client setup (axios, dayjs, query client)
├── services/     # Cross-cutting server/domain services
├── stores/       # Global state (Zustand/Redux)
├── utils/        # Pure helper functions (no React, no side effects)
├── types/        # Shared TypeScript types
└── styles/
```

## Import boundaries (enforced)

- A **feature** may import from: `components/`, `hooks/`, `lib/`, `utils/`,
  `types/`, `services/`, `stores/`, and its **own** internals.
- A **feature must NOT** import from another feature's internals — only from that
  feature's `index.ts`.
- `components/`, `hooks/`, `utils/`, `lib/` must **not** import from `features/`
  or `pages/` (shared code cannot depend on features).
- Always use the `@/` path alias, never deep relative paths like `../../../`.

## Available subagents

Delegate specialized work to these agents (in `.claude/agents/`):

- **code-reviewer** — reviews a diff or file against clean-code + SOLID rules.
- **architecture-guardian** — verifies file placement and import boundaries.
- **refactoring-specialist** — refactors code to apply the right pattern.
- **test-writer** — writes colocated tests (Vitest + Testing Library).

## Definition of done

Before you consider a coding task complete, verify:
- [ ] Files are in the correct feature/shared location.
- [ ] No cross-feature internal imports; `@/` alias used.
- [ ] Component has a single responsibility; logic extracted into hooks.
- [ ] Public functions/props/hooks are typed; no unexplained `any`.
- [ ] Names reveal intent; no dead code, no commented-out blocks.
- [ ] Errors are handled explicitly, not swallowed.
