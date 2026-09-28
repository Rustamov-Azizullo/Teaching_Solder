# "Askar ta'limi" axborot tizimi (MVP)

Muddatli harbiy xizmatchilarni kasbga va fanga o'qitish tizimi. `TEXNIK_TOPSHIRIQ.md` (v1.4) ning 12-bo'limidagi
**2-bosqich (MVP)** modullari amalga oshirilgan.

| Qism | Texnologiya | Papka |
|---|---|---|
| Frontend | React 19 + TypeScript + Vite, Ant Design, Recharts, TanStack Query | `frontend/` |
| Backend | Java 21, Spring Boot 4.0.3, Spring Security (JWT), JPA, Flyway | `backend/` |
| Baza | PostgreSQL 16 | `docker-compose.yml` |
| API hujjati | Swagger UI / OpenAPI 3 | `http://localhost:8081/swagger-ui.html` |

## Ishga tushirish

### 1. Baza
```bash
docker compose up db          # yoki o'zingizdagi PostgreSQL: baza `askar_talimi`
```
Boshqa sozlamalar uchun muhit o'zgaruvchilari: `DB_URL`, `DB_USER`, `DB_PASSWORD` (standart: `localhost:5432`, `postgres/postgres`).

### 2. Backend (port 8081)
```bash
cd backend
mvn spring-boot:run
```
Birinchi ishga tushishda Flyway sxemani yaratadi va **demo ma'lumotlar** (qismlar, askarlar, guruhlar, davomat, anketalar)
yoziladi. O'chirish: `SEED_DEMO_DATA=false`. Productionda `JWT_SECRET` ni albatta o'zgartiring.

### 3. Frontend (port 5173)
```bash
cd frontend
npm install
npm run dev
```
Dev serverda `/api` so'rovlari `http://localhost:8081` ga proksi qilinadi (`VITE_BACKEND_URL` bilan o'zgartiriladi).

### To'liq stek (Docker)
```bash
docker compose up --build     # frontend: http://localhost:8088
```

## Demo foydalanuvchilar (parol: `Parol123!`)

| Login | Rol (hudud) | Nimani ko'radi |
|---|---|---|
| `megasuperadmin` | Mega SuperAdmin (respublika) | Hammasi; ruxsatlarni boshqarish |
| `superadmin`, `admin`, `hktb`, `jtb`, `tmibb` | SuperAdmin (respublika) | Hammasi; ruxsatlarni boshqarish |
| `okrug1`, `adminuser` | Admin (1-okrug) | Faqat o'z okrugi; o'z okrugidagi foydalanuvchilarni boshqarish |
| `qomondon1`, `qomondon2` | User (101 / 201-qism) + qo'mondon ruxsatlari | Faqat o'z qismi; guruh kattasini tayinlash, standart dars vaqtini o'zgartirish |
| `operator1` | User (101-qism) + operator ruxsatlari | Askar, guruh, o'qituvchi, natija kiritish |
| `jangovar1` / `tarbiya1` | User (101-qism) + bo'lim ruxsatlari | Faqat o'z yo'nalishi dashboardi va hisobotlari |
| `katta1`, `katta2`, `katta3` | User (101 / 201-qism), guruh kattasi | Faqat o'z guruhi (telefon uchun qulay) |
| `psixolog1` | User (101-qism) + psixolog ruxsatlari | Elektron anketa |
| `user` | User (101-qism) | Faqat guruhlar va jadval (rolning standart ruxsatlari) |

Rollar faqat vakolat doirasini belgilaydi; amallar `role_permissions` (Admin/User uchun rol-ruxsat matritsasi) va
`user_permissions` (foydalanuvchiga shaxsiy qo'shimcha ruxsatlar) jadvallari orqali beriladi — SuperAdmin ularni
`/api/role-permissions` va `/api/users/{id}/permissions` orqali ish vaqtida o'zgartiradi.

JShShIR bo'yicha manba tizim hozircha **mock**: `0…` bilan boshlansa — "topilmadi", `9…` bilan boshlansa — "manba tizim
javob bermadi", qolgani uchun namunaviy ma'lumot qaytadi (`31234567890123`).

## Modullar (TT M1–M14 to'liq)

M1 ma'lumotnomalar · elektron anketa va M2 yig'ma jild (JShShIR → manba belgisi, qayta so'rovda farqlarni tasdiqlash,
XLSX ommaviy import, PDF nusxalar — AES-GCM bilan shifrlangan) · **ierarxik bo'linmalar** (batalon → rota → vzvod) va
askarni qism/bo'linmaga o'tkazish tarixi · M3 biriktirishlar (taklif → ko'rib chiqish → qaror, shartnoma) · M4 xatlov va
guruhga sinf ajratish · M5 anketa natijalari va guruhlarga taqsimot taklifi · M6 guruhlar/o'qituvchilar · M7 jadval ·
M8 davomat (tahrirlash muddati **Sozlamalarda dinamik**) · M9 kurs natijalari, sertifikatlar, qo'mondon tasdig'i,
KTA solishtirish (API + zaxira XLSX) · M10 OTMga qabul, BMBA sinxronlash, voronka, zaxiraga bo'shatish ro'yxati ·
M11 bandlik ro'yxatlari (hudud bo'yicha, XLSX/PDF, tarix) · M12 dashboard va 5 ta hisobot (XLSX/PDF) ·
M13 muddatlar nazorati va tizim ichidagi bildirishnomalar (rejalashtirilgan ishlar) · M14 rollar, audit, integratsiya
jurnali, yillik sikllar, saqlash muddati (anonimlashtirish), ixtiyoriy 2FA (TOTP), parol siyosati.

Chaqiruv bosqichi tizimga kirmaydi: askar qismga kelgach qo'lda yoki JShShIR orqali qo'shiladi (MIO roli olib tashlangan).
BMBA, KTA va manba tizim hozircha mock adapterlar (`integrations/`, `soldiers/integration/`).

## Arxitektura

`.claude/` dagi qoidalarga muvofiq:

- **Frontend** — feature-based (`src/features/<feature>/{components,hooks,api,types.ts,labels.ts,index.ts}`), boshqa
  feature faqat `index.ts` orqali import qilinadi, `@/` alias, sahifalar lazy yuklanadi. Barcha matnlar `labels.ts` /
  `lib/i18n` da (o'zbek, lotin).
- **Backend** — modul-monolit, paket-bo'yicha-feature (`soldiers`, `surveys`, `groups`, `schedule`, `attendance`,
  `dashboard`, …). Vakolat doirasi (`AccessScope`, foydalanuvchining `Location` hududidan) serverda majburlanadi; ruxsat
  tekshiruvi `Access` konstantalarida (`@perm.has(...)` — bazadagi dinamik ruxsatlar).
  Manba tizim `SoldierSourceClient` interfeysi (adapter) orqali ulanadi — haqiqiy API kelganda faqat yangi implementatsiya
  yoziladi.

## Testlar
```bash
cd backend && mvn test        # H2 (PostgreSQL rejimi) da integratsion testlar
cd frontend && npm test       # Vitest
cd frontend && npm run build  # tsc + vite build
```
