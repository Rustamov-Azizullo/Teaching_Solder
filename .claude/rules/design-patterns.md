# Dizayn Patternlar (React + TypeScript)

Dizayn pattern — tez-tez uchraydigan muammoning sinovdan o'tgan yechim namunasi.
Bu hujjatda React'da eng ko'p ishlatiladigan patternlar, ularni **qachon**
ishlatish va misollari berilgan.

> Muhim qoida: **Pattern muammoni yechish uchun, ko'rk uchun emas.** Avval eng
> oddiy yechimni sinang; pattern faqat aniq muammo (takror, prop drilling, semiz
> props) paydo bo'lganda kerak bo'ladi.

## Tez tanlash jadvali

| Muammo (belgi) | Pattern |
|---|---|
| Komponent mantiq + UI ni birga bajaradi | Custom Hook + Presentational |
| Ko'p rejim/boolean props (`isX`, `hasY`) | Composition / Compound Components |
| Props 3+ daraja pastga uzatilyapti (prop drilling) | Provider (Context) / Store |
| Bir xil mantiq 2+ joyda | Shared Custom Hook |
| Turga qarab o'sib boradigan `switch`/`if` | Strategy |
| Tashqi ma'lumot shakli ichki modelga mos emas | Adapter |
| Kirishga qarab har xil obyekt yaratish | Factory |
| Ixtiyoriy komponentni o'rash kerak (xatolik, ruxsat) | HOC / Render Props |

---

## 1. Custom Hook Pattern

**Muammo:** komponentda holat va mantiq UI bilan aralashib ketgan.
**Yechim:** mantiqni qayta ishlatiladigan hook'ga chiqaring.

```tsx
function useToggle(initial = false) {
  const [value, setValue] = useState(initial);
  const toggle = useCallback(() => setValue((v) => !v), []);
  return { value, toggle, setValue };
}

// Ishlatilishi
function Modal() {
  const { value: isOpen, toggle } = useToggle();
  return (
    <>
      <button onClick={toggle}>Ochish</button>
      {isOpen && <Dialog onClose={toggle} />}
    </>
  );
}
```

Bu React'da eng muhim va eng ko'p ishlatiladigan pattern. Mantiq hook'da bo'lsa,
uni render qilmasdan test qilsa bo'ladi.

---

## 2. Container / Presentational (Aqlli / soqov komponent)

**Muammo:** ma'lumot bilan ishlash va ko'rinish bir komponentda.
**Yechim:** "container" ma'lumotni oladi, "presentational" faqat chizadi.
Zamonaviy React'da container ko'pincha oddiy hook chaqiruvi bo'ladi.

```tsx
// Presentational — faqat props, hech qanday fetch/state yo'q
type ProductViewProps = { products: Product[]; onSelect: (id: string) => void };
function ProductView({ products, onSelect }: ProductViewProps) {
  return (
    <ul>
      {products.map((p) => (
        <li key={p.id} onClick={() => onSelect(p.id)}>{p.name}</li>
      ))}
    </ul>
  );
}

// Container — mantiqni ulaydi
function ProductListContainer() {
  const { products } = useProducts();
  const navigate = useNavigate();
  return <ProductView products={products} onSelect={(id) => navigate(`/products/${id}`)} />;
}
```

Presentational komponentlar qayta ishlatiladi, Storybook'ga qo'yiladi va oson
test qilinadi.

---

## 3. Compound Components (Qo'shma komponentlar)

**Muammo:** komponentga juda ko'p boolean/rejim props qo'shilib ketgan.
**Yechim:** bir-biriga bog'liq subkomponentlarni context orqali birlashtiring.

```tsx
const TabsContext = createContext<{ active: string; setActive: (id: string) => void } | null>(null);

function Tabs({ defaultTab, children }: PropsWithChildren<{ defaultTab: string }>) {
  const [active, setActive] = useState(defaultTab);
  const value = useMemo(() => ({ active, setActive }), [active]);
  return <TabsContext.Provider value={value}>{children}</TabsContext.Provider>;
}

function TabList({ children }: PropsWithChildren) {
  return <div role="tablist">{children}</div>;
}

function Tab({ id, children }: PropsWithChildren<{ id: string }>) {
  const ctx = useContext(TabsContext)!;
  return (
    <button role="tab" aria-selected={ctx.active === id} onClick={() => ctx.setActive(id)}>
      {children}
    </button>
  );
}

Tabs.List = TabList;
Tabs.Tab = Tab;

// Ishlatilishi — moslashuvchan va o'qish oson
<Tabs defaultTab="a">
  <Tabs.List>
    <Tabs.Tab id="a">Birinchi</Tabs.Tab>
    <Tabs.Tab id="b">Ikkinchi</Tabs.Tab>
  </Tabs.List>
</Tabs>
```

---

## 4. Provider Pattern (Context orqali)

**Muammo:** bir xil holat ko'p komponentga kerak, propslar chuqur uzatilyapti
(prop drilling).
**Yechim:** holatni context provider'ga ko'taring va guard qilingan hook bering.

```tsx
const ThemeContext = createContext<ThemeValue | null>(null);

export function ThemeProvider({ children }: PropsWithChildren) {
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const value = useMemo(() => ({ theme, setTheme }), [theme]);
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme faqat <ThemeProvider> ichida ishlaydi');
  return ctx;
}
```

Har bir context bitta mas'uliyatga ega bo'lsin (Auth, Theme alohida). Bitta
"ulkan context"ga hamma narsani tiqmang — bu keraksiz qayta renderlarni keltiradi.

---

## 5. Strategy Pattern (Strategiya)

**Muammo:** turga qarab o'sib boradigan `if`/`switch` zanjiri (OCP buziladi).
**Yechim:** har bir variantni alohida funksiya qilib, lug'atga (map) joylang.

```ts
type PaymentMethod = 'card' | 'cash' | 'crypto';

const paymentStrategies: Record<PaymentMethod, (amount: number) => Promise<Receipt>> = {
  card: (amount) => payByCard(amount),
  cash: (amount) => payByCash(amount),
  crypto: (amount) => payByCrypto(amount),
};

function processPayment(method: PaymentMethod, amount: number) {
  const strategy = paymentStrategies[method];
  return strategy(amount);
}
// Yangi to'lov turi -> map'ga bitta qator qo'shiladi, processPayment o'zgarmaydi.
```

---

## 6. Adapter Pattern (Moslashtiruvchi)

**Muammo:** tashqi API qaytargan ma'lumot shakli ilova ichki modeliga mos emas.
**Yechim:** tashqi shaklni ichki modelga aylantiruvchi adapter (mapper) yozing.

```ts
// Tashqi API shakli
type ApiUser = { user_id: number; full_name: string; is_active: 0 | 1 };

// Ichki model
type User = { id: string; name: string; isActive: boolean };

// Adapter — chegarada ma'lumotni tozalaydi
function toUser(dto: ApiUser): User {
  return {
    id: String(dto.user_id),
    name: dto.full_name,
    isActive: dto.is_active === 1,
  };
}
```

Adapter tufayli ilovaning qolgan qismi tashqi API shaklini bilmaydi — API
o'zgarsa, faqat adapter o'zgaradi (DIP bilan uyg'un).

---

## 7. Factory Pattern (Zavod)

**Muammo:** kirish parametriga qarab har xil obyekt/konfiguratsiya yaratish kerak.
**Yechim:** yaratish mantig'ini bitta "factory" funksiyaga jamlang.

```ts
type Role = 'admin' | 'editor' | 'viewer';

function createPermissions(role: Role): Permissions {
  const base: Permissions = { read: true, write: false, delete: false };
  switch (role) {
    case 'admin':  return { read: true, write: true, delete: true };
    case 'editor': return { ...base, write: true };
    case 'viewer': return base;
  }
}
```

---

## 8. HOC va Render Props (Oxirgi chora)

Zamonaviy React'da ko'p holatlar **hook** bilan yechiladi. HOC (Higher-Order
Component) yoki render props'ni faqat **ixtiyoriy komponentni o'rash** kerak
bo'lganda ishlating — masalan xatolik chegarasi, ruxsat tekshiruvi, instrumentatsiya.

```tsx
// HOC — komponentni ruxsat tekshiruvi bilan o'raydi
function withAuth<P extends object>(Component: React.ComponentType<P>) {
  return function Guarded(props: P) {
    const { user } = useAuth();
    if (!user) return <Navigate to="/login" />;
    return <Component {...props} />;
  };
}

const ProtectedDashboard = withAuth(Dashboard);
```

Agar hook yetarli bo'lsa — hook'ni tanlang. HOC ni tanlasangiz, nega hook
yetmaganini izohlang.

---

## Umumiy tamoyillar

1. **Eng oddiy yechimdan boshlang.** Pattern muammoni yechganda qo'shiladi, oldindan emas.
2. **Kompozitsiyani meros/konfiguratsiyadan afzal ko'ring** (composition over inheritance/configuration).
3. **Har bir pattern SOLID bilan uyg'un bo'lsin** — masalan Strategy → OCP, Adapter → DIP.
4. **Patternni nomi bilan ataysh** — jamoada muloqotni osonlashtiradi
   ("bu yerda Strategy ishlatdik").
