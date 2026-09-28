---
name: react-component-patterns
description: >
  Structure React components and hooks cleanly using proven patterns
  (container/presentational, custom hooks, compound components, composition over
  props, provider). USE THIS SKILL whenever writing or refactoring a component or
  hook — especially if a component fetches data AND renders UI, has many boolean
  props, deeply nested conditionals, prop drilling, or duplicated logic. Consult
  it before writing a component larger than ~50 lines or with more than a few
  responsibilities.
---

# React Component & Hook Patterns

Use these patterns to keep components small, testable, and single-purpose.
Pick the lightest pattern that solves the problem — do not over-engineer.

## 1. Separate logic from presentation (custom hooks)

The default way to give a component a single responsibility: move data fetching
and business logic into a custom hook, leave the component to render.

```tsx
// hook owns the logic
function useProductList() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['products'],
    queryFn: fetchProducts,
  });
  return { products: data ?? [], isLoading, error };
}

// component owns the rendering
function ProductList() {
  const { products, isLoading, error } = useProductList();
  if (isLoading) return <Spinner />;
  if (error) return <ErrorState error={error} />;
  return (
    <ul>
      {products.map((p) => <ProductRow key={p.id} product={p} />)}
    </ul>
  );
}
```

Benefit: the hook is unit-testable without rendering; the component is trivial to
read. This is the React-idiomatic form of the container/presentational split.

## 2. Presentational (dumb) components

Keep leaf UI components pure: props in, JSX out. No fetching, no global state, no
side effects. They are easy to reuse, snapshot, and put in Storybook.

```tsx
type ProductRowProps = { product: Product; onSelect?: (id: string) => void };

function ProductRow({ product, onSelect }: ProductRowProps) {
  return (
    <li onClick={() => onSelect?.(product.id)}>
      {product.name} — {formatPrice(product.price)}
    </li>
  );
}
```

## 3. Composition over configuration

When a component grows many boolean/mode props (`isPrimary`, `hasIcon`,
`showHeader`…), prefer composing children instead of adding flags.

```tsx
// ❌ prop explosion
<Card title="X" hasFooter footerText="OK" showClose onClose={...} />

// ✅ composition
<Card>
  <Card.Header onClose={...}>X</Card.Header>
  <Card.Body>…</Card.Body>
  <Card.Footer>OK</Card.Footer>
</Card>
```

This is the **compound component** pattern. Implement it by sharing state through
context between the parent and its subcomponents:

```tsx
const CardContext = createContext<CardCtx | null>(null);

function Card({ children }: PropsWithChildren) {
  const value = useMemo(() => ({ /* shared state */ }), []);
  return <CardContext.Provider value={value}>{children}</CardContext.Provider>;
}
Card.Header = CardHeader;
Card.Body = CardBody;
Card.Footer = CardFooter;
```

## 4. Provider pattern for shared state

When several components need the same state and you would otherwise drill props
3+ levels, lift it into a context provider (or a store). Keep the provider
focused — one concern per context (AuthProvider, ThemeProvider), not a mega-context.

```tsx
const AuthContext = createContext<AuthValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const auth = useAuthState(); // logic in a hook
  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
}
```

Always guard the consumer hook so misuse fails loudly.

## 5. Reusable logic → shared hook

If two components duplicate the same effect/state logic, extract a hook into
`src/hooks/` (global) or the feature's `hooks/` (feature-local). This is the
modern replacement for HOCs and render props for most cases.

```tsx
function useDebouncedValue<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);
  return debounced;
}
```

## 6. When to still use HOC / render props

Prefer hooks. Reach for a Higher-Order Component or render prop only for
cross-cutting concerns that must wrap arbitrary components (e.g. `withErrorBoundary`,
a permission gate wrapper, instrumentation). Document why a hook wasn't enough.

## Anti-patterns to fix on sight

- **God component**: fetches + transforms + holds state + renders complex tree.
  → Split logic into a hook and UI into presentational children.
- **Prop drilling** 3+ levels → provider/context or store.
- **Boolean prop explosion** → composition / compound components.
- **useEffect doing data fetching by hand** when a query library exists → use the
  query hook; reserve `useEffect` for genuine synchronization with external systems.
- **Business logic inside JSX** (heavy `.map/.filter/.reduce` chains in render) →
  compute in a hook or memoized selector, render the result.
- **Duplicated logic across components** → extract a shared hook.

## Selection guide

| Symptom | Pattern |
|---|---|
| Component does logic + UI | Custom hook + presentational component |
| Many mode/boolean props | Composition / compound component |
| Prop drilling | Provider (context) or store |
| Same logic in 2+ places | Shared custom hook |
| Must wrap arbitrary components | HOC / render prop (last resort) |

Keep each component under one clear responsibility. If you cannot describe what a
component does in one sentence without "and", split it.
