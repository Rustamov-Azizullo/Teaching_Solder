# SOLID Prinsiplari (React + TypeScript)

SOLID — obyektga yo'naltirilgan dizayndan kelib chiqqan 5 ta prinsip, lekin ular
React'dagi komponent, hook va servislarga ham to'liq mos keladi. Har biri quyida
React kontekstida, yomon (❌) va yaxshi (✅) misol bilan berilgan.

| Harf | Prinsip | Qisqacha |
|---|---|---|
| **S** | Single Responsibility | Bitta modul — bitta o'zgarish sababi |
| **O** | Open/Closed | Kengaytirishga ochiq, o'zgartirishga yopiq |
| **L** | Liskov Substitution | Ichki komponent asosiynikini buzmasdan almashtira olsin |
| **I** | Interface Segregation | Kichik, aniq interfeyslar; keraksiz propslar yo'q |
| **D** | Dependency Inversion | Aniq implementatsiyaga emas, abstraksiyaga bog'lan |

---

## S — Single Responsibility Principle (Yagona mas'uliyat)

**Bir komponent/hook/funksiya faqat bitta sabab bilan o'zgarishi kerak.**

React'da eng ko'p uchraydigan buzilish — bitta komponent ma'lumot yuklaydi,
uni qayta ishlaydi, holatni saqlaydi VA murakkab UI chizadi. Bularni ajrating:
mantiqni hook'ga, ko'rinishni presentational komponentga.

```tsx
// ❌ Yomon — "xudo komponent": fetch + logika + UI bir joyda
function UserDashboard() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    fetch('/api/users')
      .then((r) => r.json())
      .then((data) => {
        const active = data.filter((u) => u.isActive);
        setUsers(active);
        setLoading(false);
      });
  }, []);
  if (loading) return <div>Yuklanmoqda...</div>;
  return <div>{users.map((u) => <div key={u.id}>{u.name}</div>)}</div>;
}

// ✅ Yaxshi — mantiq hook'da, UI alohida
function useActiveUsers() {
  const { data, isLoading } = useQuery({
    queryKey: ['users', 'active'],
    queryFn: fetchUsers,
    select: (users) => users.filter((u) => u.isActive),
  });
  return { users: data ?? [], isLoading };
}

function UserDashboard() {
  const { users, isLoading } = useActiveUsers();
  if (isLoading) return <Spinner />;
  return <UserList users={users} />;
}
```

Endi mantiq o'zgarsa — hook o'zgaradi; ko'rinish o'zgarsa — komponent. Ular
mustaqil.

---

## O — Open/Closed Principle (Ochiq/yopiq)

**Modul yangi xatti-harakat qo'shishga ochiq, lekin mavjud kodni o'zgartirishga
yopiq bo'lsin.** Yangi holat qo'shilganda eski `switch`/`if` bloklarini
tahrirlashga majbur bo'lmang.

```tsx
// ❌ Yomon — har yangi tur uchun switch'ni o'zgartirish kerak
function Notification({ type, message }: Props) {
  switch (type) {
    case 'success': return <div className="green">{message}</div>;
    case 'error':   return <div className="red">{message}</div>;
    case 'warning': return <div className="yellow">{message}</div>;
    // yangi tur -> shu funksiyani yana o'zgartirish...
  }
}

// ✅ Yaxshi — konfiguratsiya orqali kengaytiriladi (kodni o'zgartirmasdan)
const NOTIFICATION_STYLES: Record<NotificationType, string> = {
  success: 'green',
  error: 'red',
  warning: 'yellow',
  info: 'blue', // yangi turni shu yerga qo'shamiz, JSX o'zgarmaydi
};

function Notification({ type, message }: Props) {
  return <div className={NOTIFICATION_STYLES[type]}>{message}</div>;
}
```

Murakkabroq holatlarda — **Strategy pattern** (qarang: `design-patterns.md`) yoki
kompozitsiya (props orqali komponent uzatish) ishlating.

---

## L — Liskov Substitution Principle (Liskov almashtirish)

**Bir abstraksiyaning har qanday implementatsiyasi, asosiy shartnomani buzmasdan,
o'rniga qo'yilishi mumkin bo'lsin.** React'da bu ko'proq komponent
"shartnomasiga" (props interfeysiga) taalluqli.

Amaliy ma'no: agar `Button` propslarini qabul qilsa, uni o'rab yasagan
`PrimaryButton` xuddi shu propslarni buzmasdan qabul qilishi va o'zini
`Button`dek tutishi kerak. Kutilgan `onClick` ni "yutib yuboradigan" yoki
`disabled` ni e'tiborsiz qoldiradigan variant LSP ni buzadi.

```tsx
// ✅ Yaxshi — o'rniga qo'ysa bo'ladigan variant
type ButtonProps = React.ComponentProps<'button'> & { variant?: 'primary' | 'ghost' };

function Button({ variant = 'primary', ...rest }: ButtonProps) {
  return <button className={variant} {...rest} />;
}

// PrimaryButton hamma standart button xatti-harakatini saqlaydi
function PrimaryButton(props: ButtonProps) {
  return <Button variant="primary" {...props} />;
}
```

Qoida: turlarni toraytiruvchi (masalan, `onClick` ni ixtiyoriy qilib qo'yib,
keyin uni ishlatmaydigan) yoki kutilmagan cheklov qo'yadigan variantlardan qoching.

---

## I — Interface Segregation Principle (Interfeys ajratish)

**Modulni unga kerak bo'lmagan narsalarga bog'liq qilib qo'ymang.** React'da bu
"semiz props" muammosi: komponentga o'nlab props berib, aksariyatini
ishlatmaslik.

```tsx
// ❌ Yomon — Avatar ga butun user obyekti kerak emas
type AvatarProps = { user: FullUser }; // 20 ta maydonli obyekt
function Avatar({ user }: AvatarProps) {
  return <img src={user.avatarUrl} alt={user.name} />;
}

// ✅ Yaxshi — faqat kerakli narsa so'raladi
type AvatarProps = { src: string; alt: string };
function Avatar({ src, alt }: AvatarProps) {
  return <img src={src} alt={alt} />;
}
```

Katta interfeysni bir nechta kichik, aniq interfeysga bo'ling. Komponent faqat
o'ziga kerak bo'lgan minimal ma'lumotni qabul qilsin — bu uni qayta ishlatishni
va test qilishni osonlashtiradi.

---

## D — Dependency Inversion Principle (Bog'liqlikni teskari qilish)

**Yuqori darajali modullar past darajali detallarga emas, abstraksiyaga bog'lansin.**
Komponent ichida to'g'ridan-to'g'ri `axios`, `localStorage` yoki aniq API ga
murojaat qilmang — ularni abstraksiya (interfeys/servis/hook) orqali oling.

```tsx
// ❌ Yomon — komponent axios va endpoint'ga qattiq bog'langan
function ProductList() {
  const [products, setProducts] = useState([]);
  useEffect(() => {
    axios.get('https://api.site.com/v1/products').then((r) => setProducts(r.data));
  }, []);
  // ... test qilish qiyin, API o'zgarsa komponent o'zgaradi
}

// ✅ Yaxshi — abstraksiyaga bog'lanadi
interface ProductRepository {
  getAll(): Promise<Product[]>;
}

// implementatsiya alohida (services/ yoki api/ da)
const httpProductRepository: ProductRepository = {
  getAll: () => apiClient.get('/products').then((r) => r.data),
};

// hook abstraksiyani oladi, aniq clientni bilmaydi
function useProducts(repo: ProductRepository = httpProductRepository) {
  return useQuery({ queryKey: ['products'], queryFn: () => repo.getAll() });
}
```

Foyda: testda soxta (`mock`) repository berish mumkin; API/HTTP kutubxonasi
o'zgarsa, komponent va hook o'zgarmaydi.

---

## Amaliy xulosa

SOLID — qat'iy qonun emas, balki yo'riqnoma. Ularni **muammo paydo bo'lganda**
qo'llang, oldindan haddan ortiq murakkablashtirmang (YAGNI). Eng ko'p foyda
beradigan ikkitasi React'da:

1. **SRP** — mantiqni hook'ga, ko'rinishni komponentga ajrating.
2. **DIP** — tashqi bog'liqliklarni (API, storage) abstraksiya orqali oling.

Bu ikkisi kodni test qilinadigan, o'zgarishga chidamli va tushunarli qiladi.
