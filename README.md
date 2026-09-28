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

| Login | Rol | Nimani ko'radi |
|---|---|---|
| `admin` | Tizim administratori | Hammasi, foydalanuvchilar, audit, sozlamalar |
| `hktb` | MV HKTB xodimi | Respublika dashboardi (3 blok), ma'lumotnomalar |
| `jtb` | MV JTB xodimi | "Kasb kurslari" dashboardi, jadval |
| `tmibb` | TMIBB xodimi | "OTM tayyorlov" dashboardi |
| `okrug1` | Harbiy okrug mas'uli | Faqat o'z okrugi |
| `qomondon1`, `qomondon2` | Qism qo'mondoni | Faqat o'z qismi; guruh kattasini tayinlash |
| `operator1` | Qism operatori | Askar, guruh, o'qituvchi kiritish |
| `jangovar1` / `tarbiya1` | Jangovar tayyorgarlik / tarbiyaviy ishlar bo'limi | Faqat o'z yo'nalishi dashboardi |
| `katta1`, `katta2`, `katta3` | Guruh kattasi | Faqat o'z guruhi davomati (telefon uchun qulay) |
| `psixolog1` | Harbiy psixolog | Elektron anketa |

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
  `dashboard`, …). Vakolat doirasi (`AccessScope`) serverda majburlanadi; rol tekshiruvi `Access` konstantalarida.
  Manba tizim `SoldierSourceClient` interfeysi (adapter) orqali ulanadi — haqiqiy API kelganda faqat yangi implementatsiya
  yoziladi.

## Testlar
```bash
cd backend && mvn test        # H2 (PostgreSQL rejimi) da integratsion testlar
cd frontend && npm test       # Vitest
cd frontend && npm run build  # tsc + vite build
```
