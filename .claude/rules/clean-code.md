# Clean Code Qoidalari (React + TypeScript)

Bu hujjat loyihada kod yozishning majburiy qoidalarini belgilaydi. Har bir
qoida qisqa tushuntirish, yomon misol (❌) va yaxshi misol (✅) bilan berilgan.
Kodni yozish yoki tekshirishdan oldin shu qoidalarga amal qiling.

> Asosiy g'oya: **Kod mashina uchun emas, odam uchun yoziladi.** Kompyuter
> istalgan kodni ishlatadi; muhimi — keyingi dasturchi (ba'zan 3 oy keyingi
> o'zingiz) uni tez tushunishi.

---

## 1. Nomlash (Naming)

Nom o'zining maqsadini oshkor qilishi kerak. Nomni o'qib, izohsiz nima ekanini
tushunib olsa bo'ladigan bo'lsin.

**Qoidalar:**
- O'zgaruvchi/funksiya nomi "nima" yoki "nima qiladi" degan savolga javob bersin.
- Qisqartma va sirli nomlardan qoching (`d`, `tmp`, `data2`).
- Boolean nomlar savol kabi bo'lsin: `isLoading`, `hasError`, `canSubmit`.
- Funksiya nomi fe'l bilan boshlansin: `getUser`, `formatPrice`, `handleSubmit`.
- Bir tushuncha uchun bitta so'z ishlating (`fetch`/`get`/`retrieve` ni aralashtirmang).

```ts
// ❌ Yomon
const d = new Date();
const list = users.filter((u) => u.a > 18);
function calc(x, y) { return x * y * 0.2; }

// ✅ Yaxshi
const currentDate = new Date();
const adultUsers = users.filter((user) => user.age > 18);
function calculateTax(price: number, quantity: number): number {
  const TAX_RATE = 0.2;
  return price * quantity * TAX_RATE;
}
```

---

## 2. Sehrli sonlar va satrlar (Magic numbers)

Kod ichida tushuntirishsiz turgan raqam yoki satrni nomli konstantaga chiqaring.

```ts
// ❌ Yomon — 0.2 va 86400000 nima?
if (Date.now() - lastLogin > 86400000) logout();
const total = price * 0.2;

// ✅ Yaxshi
const ONE_DAY_MS = 24 * 60 * 60 * 1000;
const TAX_RATE = 0.2;
if (Date.now() - lastLogin > ONE_DAY_MS) logout();
const total = price * TAX_RATE;
```

---

## 3. Funksiyalar kichik va bitta ishni bajarsin

Bitta funksiya bitta narsa qilsin (Single Responsibility). Agar funksiyani
tavsiflashda "va" so'zini ishlatsangiz — uni bo'lish kerak.

**Qoidalar:**
- Funksiya iloji boricha qisqa bo'lsin (odatda 20 qatordan oshmasin).
- Parametr soni kam bo'lsin. 3 tadan oshsa — obyektga o'rang.
- Funksiya bir darajali abstraksiyada ishlasin (yuqori mantiq va past-daraja
  detallar aralashmasin).

```ts
// ❌ Yomon — ko'p parametr + bir nechta vazifa
function createUser(name, email, age, role, isActive, sendEmail) { /* ... */ }

// ✅ Yaxshi — obyekt parametr, aniq tip
type CreateUserInput = {
  name: string;
  email: string;
  age: number;
  role: UserRole;
  isActive?: boolean;
};
function createUser(input: CreateUserInput): User { /* ... */ }
```

---

## 4. Chuqur ichma-ichlikdan qoching (Early return)

`if` larni ichma-ich joylashtirish o'rniga, shartni erta qaytish bilan yoping.

```ts
// ❌ Yomon
function getDiscount(user) {
  if (user) {
    if (user.isActive) {
      if (user.orders > 10) {
        return 0.1;
      }
    }
  }
  return 0;
}

// ✅ Yaxshi — guard clauses
function getDiscount(user: User | null): number {
  if (!user) return 0;
  if (!user.isActive) return 0;
  if (user.orders <= 10) return 0;
  return 0.1;
}
```

---

## 5. Izohlar (Comments)

**Yaxshi kod o'z-o'zini tushuntiradi.** Izoh "nima" qilayotganini emas, "nega"
qilayotganini tushuntirsin. Kodni takrorlaydigan izoh keraksiz.

```ts
// ❌ Yomon — kodni takrorlaydi
// foydalanuvchilar ro'yxatini oladi
const users = getUsers();

// i ni 1 ga oshiradi
i++;

// ✅ Yaxshi — "nega" ni tushuntiradi
// Backend 0-indeksni qo'llab-quvvatlamaydi, shuning uchun 1 dan boshlaymiz
let page = 1;
```

- Kommentga o'ralgan (o'chirilgan) kodni qoldirmang — versiya nazorati (git) bor.
- `TODO`/`FIXME` ni faqat kontekst bilan yozing: `// TODO(komilov): pagination qo'shish`.

---

## 6. DRY — takrorlanmang

Bir xil mantiqni ko'chirib yozmang. Uni funksiya, hook yoki utilga chiqaring.
Lekin **haddan oshmang**: ikkita kod tasodifan o'xshash bo'lsa, ularni majburan
birlashtirish keyin muammo tug'diradi. Takror mantiqiy jihatdan bir xil bo'lsagina
birlashtiring.

```ts
// ❌ Yomon — bir xil formatlash ikki joyda
const a = `${user.firstName} ${user.lastName}`;
const b = `${author.firstName} ${author.lastName}`;

// ✅ Yaxshi
function getFullName(person: { firstName: string; lastName: string }): string {
  return `${person.firstName} ${person.lastName}`;
}
```

---

## 7. Xatolarni ochiq boshqaring (Error handling)

Xatolarni yashirmang. Har bir mumkin bo'lgan nosozlik holatini ko'rib chiqing.

**Qoidalar:**
- Bo'sh `catch {}` yozmang — xatoni hech bo'lmasa loglang yoki qayta uzating.
- Foydalanuvchiga tushunarli holat ko'rsating (loading / error / empty).
- `try/catch` ni faqat kutilgan, boshqarib bo'ladigan xatolar uchun ishlating.

```tsx
// ❌ Yomon — xato yutib yuborilgan
try {
  await saveOrder();
} catch (e) {}

// ✅ Yaxshi
try {
  await saveOrder();
} catch (error) {
  logger.error('Buyurtmani saqlashda xatolik', { error });
  showToast('Buyurtma saqlanmadi. Qayta urinib ko\'ring.');
}
```

React komponentida har doim uch holatni qamrab oling:

```tsx
if (isLoading) return <Spinner />;
if (error) return <ErrorState message="Ma'lumot yuklanmadi" />;
if (items.length === 0) return <EmptyState />;
return <ItemList items={items} />;
```

---

## 8. TypeScript'dan to'liq foydalaning

- Public API (props, hook qaytimi, servis imzosi) aniq tiplansin.
- Sababsiz `any` ishlatmang. Kerak bo'lsa `unknown` + tekshiruv ishlating.
- Yolg'on tiplar yozmang (masalan, aslida `null` bo'lishi mumkin bo'lsa `!` bilan
  yashirmang).

```ts
// ❌ Yomon
function parse(input: any): any { return JSON.parse(input); }

// ✅ Yaxshi
function parse<T>(input: string): T {
  return JSON.parse(input) as T;
}
```

---

## 9. Mutlaq (immutable) ma'lumot bilan ishlang

State va massivlarni to'g'ridan-to'g'ri o'zgartirmang — nusxa oling. Bu React'da
qayta render va xatolar sababini kamaytiradi.

```ts
// ❌ Yomon — asl massivni o'zgartiradi
items.push(newItem);
setItems(items);

// ✅ Yaxshi
setItems([...items, newItem]);
setUser({ ...user, name: 'Yangi ism' });
```

---

## 10. O'lik kod va keraksizlikni tozalang

- Ishlatilmaydigan import, o'zgaruvchi, funksiyalarni o'chiring.
- `console.log` larni commit qilishdan oldin olib tashlang.
- "Kelajakda kerak bo'lar" degan kodni yozmang (YAGNI — hozir kerak bo'lsagina yozing).

---

## Kod tayyorligi ro'yxati (Definition of Done)

Kodni tugatishdan oldin o'zingizni tekshiring:

- [ ] Nomlar maqsadni oshkor qiladi, sirli qisqartma yo'q.
- [ ] Sehrli sonlar konstantaga chiqarilgan.
- [ ] Har bir funksiya/komponent bitta ish qiladi.
- [ ] Chuqur ichma-ich `if` yo'q; guard clause ishlatilgan.
- [ ] Izohlar "nega"ni tushuntiradi; o'chirilgan kod qoldirilmagan.
- [ ] Takror mantiq hook/util'ga chiqarilgan.
- [ ] Xatolar ochiq boshqarilgan; loading/error/empty holatlari bor.
- [ ] Public API tiplangan; sababsiz `any` yo'q.
- [ ] State immutable tarzda yangilangan.
- [ ] Ishlatilmaydigan kod va `console.log` tozalangan.
