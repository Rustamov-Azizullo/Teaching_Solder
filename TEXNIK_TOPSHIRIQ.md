# TEXNIK TOPSHIRIQ

## Harbiy qismlarda muddatli harbiy xizmatchilarni kasbga va fanga o'qitish tizimini raqamlashtirish

**Ishchi nomi:** "Askar ta'limi" axborot tizimi (keyingi o'rinlarda — **Tizim**)
**Hujjat versiyasi:** 1.5
**Asos:** "Harbiy qismlarda muddatli harbiy xizmatchilar bilan "Bitta askar — bitta kasb" tamoyili asosida kasbga o'rgatish o'quv kurslarini tashkil etish" hamda "Askarlarni oliy ta'lim muassasalariga o'qishga tayyorlash" ish algoritmlari

> **Eslatma:** Qolgan ochiq masalalar 14-bo'limda keltirilgan.

### O'zgarishlar tarixi

| Versiya | O'zgarishlar |
|---|---|
| 1.0 | Dastlabki loyiha |
| 1.1 | Davomat bo'limlarga yo'naltirilmaydi — yo'nalishlar bo'yicha dashboardda alohida diagrammalarda ko'rsatiladi; "E-tarbiya" bilan integratsiya olib tashlandi. Askar ma'lumotlari manba tizimdan **JShShIR orqali** olinadi, qolganlari tizimda to'ldiriladi. Tashqi tashkilot xodimlari tizimga kirmaydi — o'qituvchilar faqat ma'lumot sifatida kiritiladi. Fanlar ro'yxati dinamik. So'rovnomani psixolog askar bilan birgalikda o'z profilidan to'ldiradi. Bildirishnomalar faqat tizim ichida. |
| 1.2 | "Muddatli harbiy xizmatchilarning kasbiy ko'nikma va qiziqishlarini o'rganish bo'yicha anketa" asosida **elektron anketa** (6.1-bo'lim) tavsiflandi: I–III bo'limlar yig'ma jildga, IV–V bo'limlar so'rovnoma natijalariga yoziladi. Manba tizim API spetsifikatsiyasi askar ma'lumotlarini kiritish moduli ishlab chiqilayotganda taqdim etiladi — tizim JShShIR orqali qaytgan maydonlarni oldindan to'ldiradi, qolganini anketa orqali kiritadi. Dashboardga "So'rovnoma natijalari" bloki qo'shildi. |
| 1.3 | Ochiq savollar yopildi: 16-savol — erkin matn; 15-savolda tanlov rejimi sozlanadigan; BMBA va KTA bilan **API orqali** integratsiya; interfeys tili — faqat **o'zbek (lotin)**; oflayn rejim talab qilinmaydi; ma'lumotlarni saqlash muddati **sozlanadigan**; rasmiy blanklar yo'q — hisobot shakllari ishlab chiquvchi tomonidan loyihalanadi; MVP uchun qat'iy muddat mavjud. |
| 1.4 | 12-bo'limga qo'lda to'ldiriladigan MVP sanasi va kalendar reja jadvali qo'shildi. |
| 1.5 | 5-bo'lim: 12 ta qat'iy rol o'rniga 4 ta rol (Mega SuperAdmin, SuperAdmin, Admin, User), hududga biriktirish va SuperAdmin tomonidan tahrirlanadigan rol-ruxsat matritsasi hamda shaxsiy ruxsatlar. |

---

## Mundarija

1. Umumiy ma'lumotlar
2. Tizimning maqsadi va vazifalari
3. Atamalar va qisqartmalar
4. Avtomatlashtiriladigan jarayonlar tavsifi
5. Foydalanuvchilar va rollar
6. Funksional talablar (modullar)
7. Integratsiya talablari
8. Ma'lumotlar modeli
9. Nofunksional talablar
10. Axborot xavfsizligi talablari
11. Texnologik stek va arxitektura
12. Ishlab chiqish bosqichlari
13. Sinov va qabul qilish tartibi
14. Qabul qilingan qarorlar va qolgan ochiq masalalar

---

## 1. Umumiy ma'lumotlar

### 1.1. Loyihaning mohiyati

Hozirgi kunda muddatli harbiy xizmatchilarni kasbga o'qitish va oliy ta'limga tayyorlash jarayoni ko'plab tashkilotlar (Mudofaa vazirligi, harbiy okruglar, harbiy qismlar, texnikumlar, maktablar, Kasbiy ta'lim agentligi, hokimliklar va boshqalar) ishtirokida, asosan qog'oz hujjatlar va alohida hisobotlar orqali olib boriladi. Tizim ushbu jarayonni yagona raqamli muhitga ko'chiradi: chaqiruvdan boshlab xizmat yakunlanib, askar ishga joylashguniga qadar bo'lgan butun yillik siklni kuzatib boradi.

Tizimdan faqat Mudofaa vazirligi tizimidagi mas'ul xodimlar foydalanadi. Tashqi tashkilotlar (texnikum, maktab, hokimlik va boshqalar) tizimga kirmaydi: ularning o'qituvchilari haqidagi ma'lumotlar qism xodimlari tomonidan kiritiladi, idoralarga uzatiladigan ro'yxatlar esa tizimdan eksport qilinadi.

### 1.2. Tizim qamrab oladigan ikki yo'nalish

| Yo'nalish | Mazmuni | Davomiyligi |
|---|---|---|
| **A. Kasbga o'qitish** ("Bitta askar — bitta kasb") | Biriktirilgan texnikum o'qituvchilari tomonidan harbiy qism hududida 6 oygacha bo'lgan kasb kurslari, imtihon va sertifikat | Fevral — keyingi yil fevral |
| **B. OTMga tayyorlash** | Biriktirilgan maktab/o'quv markazi o'qituvchilari tomonidan asosiy fanlar bo'yicha tayyorlov kurslari (algoritmda — 5 ta fan; tizimda fanlar ro'yxati dinamik), BMBA testlari, qabul va onlayn o'qish | Fevral — keyingi yil fevral |

### 1.3. Buyurtmachi va manfaatdor tomonlar

- **Buyurtmachi:** Mudofaa vazirligi (Harbiy kadrlarni tayyorlash boshqarmasi).
- **Tizim foydalanuvchilari:** Mudofaa vazirligi boshqarmalari, harbiy okruglar, harbiy qismlar, mudofaa ishlari organlari xodimlari.
- **Tizimga kirmaydigan ishtirokchilar:** Kasbiy ta'lim agentligi va texnikumlar, maktabgacha va maktab ta'limi boshqarmalari, maktablar va o'quv markazlari, Bilim va malakalarni baholash agentligi (BMBA), Bandlik va kambag'allikni qisqartirish vazirligi, Migratsiya agentligi, Yoshlar ishlari agentligi, O'zbekiston Mahallalar uyushmasi, Qoraqalpog'iston Respublikasi Vazirlar Kengashi va hokimliklar.

---

## 2. Tizimning maqsadi va vazifalari

### 2.1. Maqsad

Muddatli harbiy xizmatchilarni kasbga va oliy ta'limga tayyorlash jarayonini raqamlashtirish, uni barcha boshqaruv darajalarida real vaqt rejimida monitoring qilish hamda xizmatdan qaytgan yoshlarning bandligini ta'minlash uchun ishonchli ma'lumotlar bazasini shakllantirish.

### 2.2. Asosiy vazifalar

1. Har bir askarning yagona elektron **shaxsiy yig'ma jildini** yuritish (ma'lumoti, sertifikatlari, yutuqlari, o'qish tarixi).
2. Askar ma'lumotlarini **JShShIR kiritish orqali** manba tizimdan olish va yetishmagan ma'lumotlarni tizimda to'ldirish.
3. Texnikum va maktablarni harbiy qismlarga **biriktirish**, shartnoma va qo'shma rejalarni qayd etish.
4. O'quv guruhlari, o'qituvchilar (ma'lumot sifatida) va dars jadvalini elektron yuritish.
5. So'rovnomalar orqali OTMga kirish istagidagi nomzodlarni va qiziqqan kasblarni aniqlash.
6. Kurs natijalari, imtihonlar va **sertifikatlarni** hisobga olish.
7. Qism → okrug → vazirlik darajasida **dashboardlar** (yo'nalishlar bo'yicha alohida diagrammalar bilan) va hisobotlar shakllantirish.
8. Bandlik uchun askarlar ro'yxatlarini tegishli idoralar va hokimliklarga yuborish uchun **eksport qilish**.
9. Algoritmda belgilangan **muddatlarga rioya etilishini nazorat qilish** va tizim ichida ogohlantirishlar berish.

### 2.3. Kutilayotgan natijalar

- Qog'oz hisobotlar va qo'lda umumlashtirish ishining keskin qisqarishi.
- Vazirlik rahbariyati uchun istalgan paytda dolzarb statistika.
- Har bir askarning ta'lim yo'lini chaqiruvdan ishga joylashgunga qadar kuzatish imkoniyati.

---

## 3. Atamalar va qisqartmalar

| Qisqartma | To'liq nomi |
|---|---|
| MV | Mudofaa vazirligi |
| HKTB | Harbiy kadrlarni tayyorlash boshqarmasi |
| JTB | Jangovar tayyorgarlik boshqarmasi |
| TMIBB | Tarbiyaviy va mafkuraviy ishlar bosh boshqarmasi |
| MBMM | Mudofaani boshqarish milliy markazi |
| MIO | Mudofaa ishlari organlari |
| HMS | Harbiy-ma'muriy sektor |
| BMBA | Bilim va malakalarni baholash agentligi |
| OTM | Oliy ta'lim muassasasi |
| JShShIR | Jismoniy shaxsning shaxsiy identifikatsiya raqami |
| RBAC | Rolga asoslangan kirish nazorati (Role-Based Access Control) |
| Guruh kattasi | O'quv/tayyorlov guruhiga buyruq bilan biriktirilgan ofitser yoki kontrakt asosidagi serjant |
| Yig'ma jild | Askarning elektron shaxsiy ishi (hujjatlar va ma'lumotlar to'plami) |
| Manba tizim | Chaqiriluvchilar ma'lumotlari yuritiladigan tashqi tizim; JShShIR bo'yicha so'rovga javob beradi |

---

## 4. Avtomatlashtiriladigan jarayonlar tavsifi

Tizim yillik siklga bog'langan bosqichli jarayonni avtomatlashtiradi. Har bir bosqich tizimda alohida **"bosqich holati"** sifatida aks etadi va unga tegishli funksiyalar faqat o'z muddatida faol bo'ladi.

### 4.1. "A" yo'nalishi — Kasbga o'qitish

| Bosqich | Davr | Asosiy harakatlar | Tizimdagi aksi |
|---|---|---|---|
| 1 | Fevral — mart | Targ'ibot; chaqiriluvchilar hujjatlarini (sertifikat, diplom, yutuqlar) aniqlash va PDF nusxalarini yig'ma jildga kiritish; **1 fevralgacha** texnikumlarni qismlarga biriktirish takliflari; **1 aprelgacha** qo'shma qaror, kasblar ro'yxati va o'quv dasturlari; **20 martgacha** kun tartibi va vaqt taqsimoti | JShShIR orqali ma'lumot olish va to'ldirish (M2), biriktirish takliflari (M3), ma'lumotnomalar (M1), muddat nazorati (M13) |
| 2 | Aprel | Boshlang'ich harbiy tayyorgarlik davrida ma'lumot berish; psixolog so'rovnomasi; guruhlar shakllantirish | So'rovnoma (M5), guruhlar (M6) |
| 3 | May — dekabr | **1 maydan** kurslar boshlanadi; guruh kattasi biriktiriladi; har kuni **15:00–17:25 (3 o'quv soati)** mashg'ulotlar; kurs yakuni → HKTB | Jadval (M7), natijalar va sertifikatlar (M9), dashboard — "Kasb kurslari" diagrammalari (M12) |
| 4 | Yanvar — fevral | Xizmat tugashiga bir oy qolganda o'qitilgan askarlar ro'yxatlarini bandlik bo'yicha idoralar va hokimliklarga yuborish | Bandlik ro'yxatlari eksporti (M11) |

### 4.2. "B" yo'nalishi — OTMga tayyorlash

| Bosqich | Davr | Asosiy harakatlar | Tizimdagi aksi |
|---|---|---|---|
| 1 | Fevral — mart | Targ'ibot; til, fan va milliy sertifikatlar, yutuqlar va oliy ma'lumot hujjatlarini aniqlash, PDF nusxalarini bazaga kiritish | M2 (JShShIR orqali + qo'lda to'ldirish) |
| 2 | Aprel | OTMga kirish tartibi haqida ma'lumot; psixolog so'rovnomasi; tayyorlov guruhlari va nomzodlar ro'yxati; maktablar/o'quv markazlarini biriktirish; ikki tomonlama shartnoma va qo'shma reja; fanlar bo'yicha o'qituvchilar ro'yxati; o'qituvchilarning qismga kirish-chiqish tartibi; sinflar ajratish | M5, M6, M3 |
| 3 | May — iyun | **1 maydan** tayyorlov kurslari; guruh kattasi; har kuni 15:00–17:25; iyunda nomzodlarni **BMBA platformasida** ro'yxatdan o'tkazish va imtiyozlarni yuklash nazorati | M7, M10, dashboard — "OTM tayyorlov kurslari" diagrammalari (M12) |
| 4 | Iyul | BMBA test sinovlarida ishtirokni ta'minlash; natijalar va qabul qilinganlarni umumlashtirib HKTBga yuborish | M10, M12 |
| 5 | Sentabr — fevral | OTMga kirganlar uchun onlayn o'qishni tashkil etish; fevralda ularni **muddatidan bir oy oldin zaxiraga bo'shatish** | M10, M11 |

---

## 5. Foydalanuvchilar va rollar

Tizimga faqat Mudofaa vazirligi tizimidagi xodimlar kiradi. Tizim ierarxik tuzilishga ega: har bir foydalanuvchi **hududga** (respublika → harbiy okrug → harbiy qism) biriktiriladi va faqat o'z **vakolat doirasidagi** ma'lumotlarni ko'radi.

Tizimda **4 ta rol** mavjud. Rol foydalanuvchining vakolat doirasini (darajasini) belgilaydi; aniq amallar esa **ruxsatlar** (masalan, "Askarlarni kiritish", "Kurs natijalarini ko'rish", "Guruh kattasini tayinlash") orqali beriladi.

| № | Rol | Vakolat doirasi | Hududga biriktirish | Ruxsatlar |
|---|---|---|---|---|
| 1 | Mega SuperAdmin | Respublika | Biriktirilmaydi | Barcha ruxsatlar — doimiy, o'zgartirib bo'lmaydi |
| 2 | SuperAdmin | Respublika | Biriktirilmaydi | Barcha ruxsatlar — doimiy, o'zgartirib bo'lmaydi; rol-ruxsat matritsasini va shaxsiy ruxsatlarni boshqaradi |
| 3 | Admin | Okrug | Harbiy okrug | Rol-ruxsat matritsasi bo'yicha (standart: o'z okrugidagi foydalanuvchilarni boshqarish, okrug dashboardlari va hisobotlari, biriktirish takliflari, bandlik ro'yxatlari) |
| 4 | User | Qism | Harbiy qism | Rol-ruxsat matritsasi bo'yicha (standart: guruhlar va jadvalni ko'rish) + foydalanuvchiga berilgan shaxsiy ruxsatlar |

**Ruxsatlar qanday belgilanadi:**

- **Rol-ruxsat matritsasi** — Admin va User rollari uchun qaysi ruxsatlar berilishini SuperAdmin tizim ishlayotgan paytda (dasturni o'zgartirmasdan) tahrirlaydi. O'zgarish darhol, qayta kirishsiz kuchga kiradi.
- **Shaxsiy ruxsatlar** — foydalanuvchiga rol ruxsatlari ustiga qo'shimcha ruxsat beriladi (faqat qo'shadi, olib tashlamaydi). Qism darajasidagi turli vazifalar shu orqali ifodalanadi: masalan, qism qo'mondoni — askarlar, guruhlar, natijalar, askarni o'tkazish, guruh kattasini tayinlash va standart dars vaqtini o'zgartirish; qism operatori — ma'lumotlarni kiritish; jangovar tayyorgarlik / tarbiyaviy ishlar bo'limi — tegishli dashboard va hisobotlar; harbiy psixolog — so'rovnomalar; guruh kattasi — askarlar ro'yxatini ko'rish.
- **Guruh kattasi** alohida rol emas: guruhga kattasi etib tayinlangan va guruhlarni boshqarish ruxsati bo'lmagan foydalanuvchi faqat o'z guruh(lar)ini ko'radi.
- SuperAdmin va Mega SuperAdmin har doim barcha ruxsatlarga ega; ruxsatlarni boshqarish ham faqat ularga ochiq.

> **Tizimga kirmaydiganlar:** texnikum va maktab o'qituvchilari, muassasa rahbarlari, KTA, hokimliklar va boshqa idoralar. O'qituvchilar tizimda faqat ma'lumot yozuvi sifatida mavjud (F.I.Sh., mutaxassisligi, qaysi tashkilotdan kelgani). Ular tomonidan beriladigan ma'lumotlar (o'qituvchilar ro'yxati, imtihon natijalari va h.k.) qism xodimlari tomonidan kiritiladi.

---

## 6. Funksional talablar (modullar)

### 6.1. Elektron anketa (askar ma'lumotlarini kiritishning asosiy formasi)

Askar haqidagi ma'lumotlar amaldagi qog'oz **"Muddatli harbiy xizmatchilarning kasbiy ko'nikma va qiziqishlarini o'rganish bo'yicha anketa"** tuzilmasi asosidagi elektron forma orqali kiritiladi. Anketa psixolog profilidan, askar bilan suhbat davomida to'ldiriladi (M5). Forma ikki manbadan to'ladi:

- **JShShIR orqali avtomatik** — manba tizim qaytargan maydonlar oldindan to'ldiriladi va "integratsiya" belgisi bilan ko'rsatiladi;
- **qo'lda** — manba tizimda bo'lmagan maydonlar psixolog tomonidan askardan so'rab kiritiladi.

Qaysi maydonlar manba tizimdan kelishi askar ma'lumotlarini kiritish moduli ishlab chiqilayotganda taqdim etiladigan API spetsifikatsiyasi asosida belgilanadi. Shuning uchun **anketaning barcha maydonlari qo'lda kiritishga tayyor** bo'lishi, avtomatik to'ldirish esa ular ustiga "qatlam" sifatida ishlashi kerak.

**Anketa sarlavhasi (avtomatik):** to'ldirilgan sana, harbiy okrug (birlashma), harbiy qism (muassasa) — to'ldirayotgan psixologning profilidan olinadi.

#### I. Umumiy ma'lumotlar → yig'ma jild

| № | Maydon | Turi | Majburiy | Izoh |
|---|---|---|---|---|
| 5.1 | JShShIR (PINFL) | Matn, 14 raqam | Ha | Formani ochishda birinchi kiritiladi; shu bo'yicha manba tizimga so'rov yuboriladi |
| 1 | F.I.Sh. | Matn | Ha | |
| 2 | Yashash manzili | Tarkibiy maydon | Ha | Hudud (viloyat) → tuman (shahar) — ma'lumotnomadan tanlanadi; MFY, ko'cha, uy, xonadon — matn |
| 3 | Tug'ilgan sanasi | Sana (kun/oy/yil) | Ha | |
| 4 | Telefon raqami | +998 formatida | Ha | Ota-ona yoki yaqin qarindoshining raqami; qarindoshlik darajasi ham ko'rsatiladi |
| 5 | Pasport (ID-karta) seriyasi va raqami | Matn (seriya + raqam) | Ha | Format tekshiriladi |

#### II. Ta'lim ma'lumoti → yig'ma jild

| № | Maydon | Turi | Variantlar |
|---|---|---|---|
| 6 | Umumiy o'rta ta'lim | Tanlov (bittasi) | maktab; akademik litsey |
| 7 | Professional ta'lim | Tanlov (bittasi, ixtiyoriy) | kasb-hunar maktabi; texnikum; kollej |
| 8 | Oliy ta'lim | Tanlov (bittasi, ixtiyoriy) | bakalavr; magistratura; tugallanmagan oliy |
| 9 | Til sertifikati | Ha/Yo'q + tafsilot | "Ega" tanlansa — qaysi tildan (ro'yxatdan), darajasi va PDF nusxasi (ixtiyoriy) |

#### III. Kasbiy ko'nikmalari → yig'ma jild

| № | Maydon | Turi | Variantlar |
|---|---|---|---|
| 10 | Xizmatga qadar egallagan kasb/hunari yoki faoliyati | Matn yoki "shug'ullanmagan" | — |
| 11 | Sovrindorligi | Bir nechta tanlov + o'rin | respublika sport musobaqasi; respublika fan olimpiadasi; xalqaro sport musobaqasi; xalqaro fan olimpiadasi. Har biri uchun o'rin (I, II, III, gran-pri) va PDF nusxa (ixtiyoriy) |
| 12 | Kasb/hunar sertifikati | Ha/Yo'q + tafsilot | "Ega" — qaysi kasb/hunar, PDF nusxa (ixtiyoriy) |
| 13 | Fan sertifikati | Ha/Yo'q + tafsilot | "Ega" — qaysi fandan (dinamik fanlar ro'yxatidan), PDF nusxa (ixtiyoriy) |

#### IV. Qiziqishlari → so'rovnoma natijalari

| № | Savol | Turi | Variantlar |
|---|---|---|---|
| 14 | Sizni ko'proq qiziqtiradigan **bitta** kasb yo'nalishi | Tanlov (faqat bittasi) | "Kasb yo'nalishlari" ma'lumotnomasidan (quyida). "Boshqa" tanlansa — matn majburiy |
| 15 | Kelgusida nima ish bilan shug'ullanmoqchisiz? | Tanlov — rejimi sozlanadigan (bitta yoki bir nechta; standart — bir nechta), administrator anketa sozlamalarida belgilaydi | xususiy sektorda doimiy ishga joylashish; davlat sektorida doimiy ishga joylashish; tadbirkorlik; **oliy ta'limga o'qishga kirish**; xorijda ishlash; kasb-hunarni egallash; harbiy xizmatni davom ettirish; dehqonchilik; hunarmandchilik; boshqa (matn) |

**"Kasb yo'nalishlari" boshlang'ich ro'yxati** (anketadagidek): avtomobillarga texnik xizmat ko'rsatish va mashinist (avtokran, ekskavator); xizmat ko'rsatish sohasi (sartarosh, oshpaz, qandolatchi, novvoy); qurilish sohasi (suvoqchi, betonchi, g'isht teruvchi, plitkachi, kafel ishlari); bo'yoqchilik va ta'mirlash (bo'yoqchi, bezakchi, gipsokarton); duradgorlik (mebel yasash va ta'mirlash, pol yotqizish); dehqonchilik (issiqxona, limonchilik, bog'dorchilik); kompyuter, IT va dasturlash; til kurslari (ingliz, nemis, koreys); elektromontaj va elektronika; buxgalteriya va tadbirkorlik (biznes, savdo); servis xizmatlari (ofitsiant, santexnik, tokar); chorvachilik (baliqchilik, asalarichilik, quyonchilik); boshqa.

> Anketadagi eslatmaga muvofiq, bu ro'yxat **harbiy qismga biriktirilgan texnikumlar imkoniyatidan kelib chiqib o'zgartirilishi mumkin**. Shuning uchun ro'yxat dinamik ma'lumotnoma bo'ladi: umumiy ro'yxat markazda yuritiladi, har bir qism uchun esa undan faol yo'nalishlar tanlanadi (qo'shimcha yo'nalish qo'shish ham mumkin). Anketada askarga faqat o'z qismi uchun faol yo'nalishlar ko'rsatiladi.

#### V. Oliy ta'lim borasidagi rejalar → so'rovnoma natijalari

Bu bo'lim **faqat 15-savolda "oliy ta'limga o'qishga kirish" tanlanganda** ochiladi va unda barcha bandlar to'ldirilishi majburiy bo'ladi.

| № | Savol | Turi | Izoh |
|---|---|---|---|
| 16 | Qaysi OTM va ta'lim yo'nalishiga hujjat topshiradi | Ro'yxat, 3 tagacha, ustuvorlik tartibida (1-, 2-, 3-) | Har bir qatorda: OTM nomi va ta'lim yo'nalishi (mutaxassislik) — **erkin matn** sifatida kiritiladi |
| 17 | Tanlangan yo'nalish uchun ixtisoslik fanlari | Bir nechta tanlov | Dinamik fanlar ro'yxatidan (boshlang'ich: matematika, fizika, kimyo, biologiya, geografiya, tarix, chet tili — qaysi tilligi ko'rsatiladi); "boshqa" — matn |
| 18 | Majburiy fanlarning qaysi biridan qo'shimcha tayyorgarlik zarur | Bir nechta tanlov | Dinamik ro'yxatdan (boshlang'ich: ona tili (o'zbek tili), matematika, O'zbekiston tarixi) |

#### Anketani yakunlash

- Saqlashdan oldin tizim barcha majburiy bandlarni tekshiradi (anketadagi "bandlarni to'liq to'ldiring" talabi) va to'ldirilmaganlarini ko'rsatadi.
- Anketa **qoralama** sifatida saqlanishi va keyinroq davom ettirilishi mumkin; yakunlangandan keyin tahrirlash faqat psixolog yoki qism qo'mondoni ruxsati bilan, o'zgarishlar tarixi saqlanadi.
- Yakunlangan anketani qog'oz shaklidagi ko'rinishda **PDF sifatida chop etish** imkoniyati bo'ladi (askar va psixolog imzosi uchun); imzolangan skan nusxani yuklab qo'yish mumkin.
- Anketa har bir yillik siklda bir marta to'ldiriladi; takroriy to'ldirishda oldingi javoblar ko'rsatiladi.

### M1. Ma'lumotnomalar (spravochniklar)

- Harbiy okruglar, harbiy qismlar (tarkibiy bo'linmalari bilan), viloyat/tumanlar.
- Texnikumlar, maktablar va o'quv markazlari (davlat va xususiy).
- Tasdiqlangan **kasblar ro'yxati** (qo'shma qaror bilan) va har bir kasb uchun o'quv dasturi.
- **Fanlar ro'yxati — dinamik:** administrator (yoki vakolatli rol) fanlarni qo'shadi, tahrirlaydi, faolsizlantiradi. Har bir fan uchun: nomi, kodi, tayyorlov dasturi, soatlar hajmi. Algoritmdagi "5 ta asosiy fan" boshlang'ich to'plam sifatida kiritiladi, lekin tizimda fanlar soni cheklanmaydi va kodga "qattiq" yozilmaydi.
- Sertifikat turlari (til, fan, milliy), yutuq turlari (sport, olimpiada, ko'rik-tanlov), ma'lumot darajalari — ham dinamik ma'lumotnomalar.
- Anketa uchun ma'lumotnomalar (6.1-bo'lim): **kasb yo'nalishlari** (markaziy ro'yxat + qism bo'yicha faollashtirish), **kelgusi reja variantlari**, ta'lim muassasasi turlari, tillar, sovrindorlik darajalari, qarindoshlik darajalari, viloyat va tumanlar.
- Kun tartibi shablonlari (standart: 15:00–17:25, 3 o'quv soati).
- Ma'lumotnomalar yillik sikl bo'yicha **versiyalanadi**: faolsizlantirilgan fan yoki kasb o'tgan yil ma'lumotlari va hisobotlarida saqlanib qoladi.

### M2. Askarning shaxsiy yig'ma jildi

Yig'ma jild asosan elektron anketaning I–III bo'limlari orqali to'ldiriladi (6.1-bo'lim). Mudofaa ishlari organi xodimi chaqiruv bosqichida ham shu formaning I–III bo'limlarini to'ldirishi mumkin; bu holda psixolog anketani ochganda ular tayyor holda ko'rinadi.

**Ma'lumotni olish tartibi:**

1. Xodim askarning **JShShIR**ini kiritadi.
2. Tizim manba tizimga so'rov yuboradi va olingan ma'lumotlar bilan yig'ma jildni avtomatik to'ldiradi.
3. Manba tizimda mavjud bo'lmagan yoki yetishmagan ma'lumotlarni xodim tizimning o'zida qo'shib chiqadi.
4. Manba tizimdan olingan maydonlar "integratsiya" belgisi bilan ko'rsatiladi; qo'lda qo'shilganlari — "qo'lda" belgisi, kiritgan xodim va sana bilan.
5. JShShIR qayta so'ralganda manba tizimdagi ma'lumot yangilangan bo'lsa, farqlar xodimga ko'rsatiladi va u tasdiqlagandan keyingina yangilanadi.
6. Manba tizim javob bermasa yoki JShShIR topilmasa, xodim barcha ma'lumotlarni qo'lda kiritishi mumkin (JShShIR formati tekshiriladi, takroriy yozuvga yo'l qo'yilmaydi).

**Yig'ma jild tarkibi:**

- Asosiy ma'lumotlar (anketa I bo'limi): F.I.Sh., JShShIR, pasport seriyasi va raqami, tug'ilgan sana, yashash manzili (viloyat, tuman/shahar, MFY, ko'cha, uy, xonadon), ota-ona yoki yaqin qarindoshining telefon raqami.
- Xizmat ma'lumotlari: chaqiruv sanasi, xizmat o'tayotgan okrug va qism, xizmat tugash sanasi.
- Ta'lim ma'lumoti (anketa II bo'limi): umumiy o'rta, professional va oliy ta'lim; til sertifikati.
- Kasbiy ko'nikmalar (anketa III bo'limi): xizmatga qadar kasb/faoliyat, sovrindorlik, kasb va fan sertifikatlari — tasdiqlovchi **PDF nusxalar** bilan.
- Kasbga o'quvchanligi haqida belgi.
- So'rovnoma natijalari (anketa IV–V bo'limlari) — M5 da saqlanadi va yig'ma jildda ko'rinadi.
- Ta'lim yo'li tarixi: qaysi guruhda o'qigan, natija, sertifikat, OTM natijasi, bandlik ro'yxatiga kiritilganligi.
- Askar boshqa qismga o'tkazilganda yig'ma jild u bilan birga ko'chadi (o'tkazish tarixi saqlanadi).
- Qidiruv va filtrlash: JShShIR, F.I.Sh., qism, kasb, sertifikat turi, holat bo'yicha.

### M3. Biriktirishlar va shartnomalar

- Okrug/qism tomonidan **texnikumni qismga biriktirish taklifi** kiritiladi → HKTB ko'rib chiqadi → qo'shma qaror asosida tasdiqlanadi (holatlar: `taklif` → `ko'rib chiqilmoqda` → `tasdiqlangan` / `rad etilgan`).
- **Maktab / o'quv markazini qismga biriktirish** (qo'shma qaror yoki buyruq asosida).
- Qism va maktab o'rtasidagi **ikki tomonlama shartnoma** va **qo'shma reja** — rekvizitlari va skan nusxasi bilan (qism xodimi yuklaydi).
- Muassasa tomonidan qog'ozda taqdim etilgan **o'qituvchilar ro'yxati** qism xodimi tomonidan tizimga kiritiladi (M6).

### M5. So'rovnomalar

- Asosiy so'rovnoma — **elektron anketa** (6.1-bo'lim). Uning IV (qiziqishlar) va V (oliy ta'lim rejalari) bo'limlari so'rovnoma natijasi sifatida saqlanadi.
- **To'ldirish tartibi:** anketa psixologning profilidan to'ldiriladi — psixolog askar bilan suhbat davomida savollarni beradi va askarning javoblarini kiritadi. Askar tizimga alohida kirmaydi.
- Har bir to'ldirilgan anketada askar, psixolog va sana qayd etiladi.
- Anketa savollari va variantlari ma'lumotnomalarga bog'langan, shuning uchun variantlar kodni o'zgartirmasdan yangilanadi. Kelajakda qo'shimcha so'rovnomalar kerak bo'lsa, psixolog yoki markaz yangi shablon yaratishi mumkin.
- Natijalar avtomatik umumlashtiriladi:
  - qiziqqan kasb yo'nalishlari (14-savol) → kasb kurslari guruhlarini shakllantirish uchun;
  - kelgusi rejalar (15-savol) → bandlik va OTM yo'nalishlarini rejalashtirish uchun;
  - OTMga topshiruvchilar, ixtisoslik fanlari (17-savol) va qo'shimcha tayyorgarlik kerak bo'lgan majburiy fanlar (18-savol) → OTM tayyorlov guruhlarini shakllantirish uchun.
- Natija asosida **nomzodlar ro'yxati** va guruhlarga taqsimlash taklifi shakllanadi: masalan, 14-savolda bir xil yo'nalishni tanlagan askarlar bitta kasb guruhiga, 18-savolda bir xil fanni belgilaganlar tegishli fan guruhiga taklif qilinadi. Yakuniy taqsimotni qism xodimi tasdiqlaydi.

### M6. Guruhlar va o'qituvchilar

- Guruh turi: `kasb kursi` yoki `OTM tayyorlov kursi`.
- Guruh parametrlari: kasb yoki fan(lar) (dinamik ro'yxatdan), qism, muassasa, boshlanish va tugash sanasi (kasb kurslari uchun **6 oygacha**), sinf.
- **Guruh kattasini** tayinlash (buyruq raqami va sanasi bilan).
- **O'qituvchilar — faqat ma'lumot yozuvi** (tizim foydalanuvchisi emas). Kiritiladigan ma'lumotlar:
  - F.I.Sh.;
  - mutaxassisligi (o'qitadigan kasb yoki fan);
  - qaysi tashkilotdan kelgani (texnikum / maktab / o'quv markazi — ma'lumotnomadan).
- O'qituvchini guruhlarga biriktirish.
- O'qituvchilarning **qismga kirish-chiqish ruxsati**: buyruq rekvizitlari, amal qilish muddati; muddat tugashidan oldin tizim ichida ogohlantirish.
- O'qituvchining mashg'ulotga kelgan-kelmagani guruh kattasi tomonidan qayd etiladi.

### M7. Dars jadvali va kun tartibi

- JTB tomonidan tasdiqlangan kun tartibi va vaqt taqsimoti asosida jadval tuziladi.
- Standart vaqt: **15:00–17:25, 3 o'quv soati**; o'zgartirish faqat vakolatli rol tomonidan, sababi ko'rsatilib.
- Nazariy va amaliy mashg'ulotlarni ajratish.
- Bayram, dala mashg'ulotlari va boshqa sabablarga ko'ra mashg'ulot bekor qilinishini qayd etish.
- O'tilgan mashg'ulotlar soni va mavzusi guruh kattasi tomonidan kiritiladi.

### M9. Kurs yakuni, imtihonlar va sertifikatlar

- Kurs yakunida imtihon natijalari **qism mas'ul xodimi** tomonidan kiritiladi (texnikum taqdim etgan qaydnoma asosida; qaydnoma skanini yuklash imkoniyati bilan).
- Holatlar: `o'qidi`, `imtihondan o'tdi`, `sertifikat oldi`, `o'qishni tugatmadi` (sababi bilan).
- Sertifikat rekvizitlari (raqami, sanasi, bergan muassasa) va PDF nusxasi.
- Qism qo'mondoni kurs yakunini tasdiqlaydi → ma'lumot HKTB dashboardida aks etadi.

### M10. OTMga qabul jarayoni

- Nomzodlar ro'yxati (so'rovnoma va guruhlar asosida).
- **BMBA platformasida ro'yxatdan o'tganlik** holati va imtiyozlar yuklanganligi (iyun) — BMBA **API** orqali JShShIR bo'yicha tekshiriladi.
- Test sinovida ishtiroki (iyul) va test natijalari (ball) — BMBA API orqali avtomatik olinadi; API javob bermasa, qism xodimi qo'lda kiritadi.
- Qabul natijasi: OTM nomi, yo'nalishi, ta'lim shakli.
- Sentabrdan: **onlayn o'qish** holati (OTM bilan kelishilganligi).
- Fevral: **muddatidan bir oy oldin zaxiraga bo'shatish** uchun ro'yxat shakllantirish.

### M11. Bandlik uchun ro'yxatlar

- Xizmat tugashiga **bir oy qolganda** tizim kasbga o'qitilgan askarlar ro'yxatini avtomatik tayyorlaydi (o'qiganlar soni, imtihondan o'tganlar, sertifikat olganlar).
- Ro'yxatlar askarning **doimiy yashash hududi** bo'yicha guruhlanadi (Qoraqalpog'iston Respublikasi, viloyatlar, Toshkent shahri).
- Qabul qiluvchilar (Bandlik vazirligi, Migratsiya agentligi, Yoshlar ishlari agentligi, Mahallalar uyushmasi, hokimliklar) tizimga kirmagani sababli ro'yxatlar **XLSX va PDF** shaklida eksport qilinadi va rasmiy tartibda yuboriladi.
- Eksportda faqat zarur minimal maydonlar bo'ladi (xizmat tafsilotlari va qism ma'lumotlarisiz).
- Qaysi ro'yxat qachon, qaysi idoraga tayyorlangani tizimda qayd etiladi.

### M12. Monitoring, dashboard va hisobotlar

**Dashboard tuzilishi.** Dashboard rolga qarab qism / okrug / respublika darajasida ochiladi va yo'nalishlar bo'yicha **alohida bloklarga** bo'linadi.

**1-blok. Kasb kurslari ("Bitta askar — bitta kasb")**

| Diagramma | Turi | Mazmuni |
|---|---|---|
| Kurs natijalari | Ustunli (yig'ma) | O'qiganlar, imtihondan o'tganlar, sertifikat olganlar |

**2-blok. OTM tayyorlov kurslari**

| Diagramma | Turi | Mazmuni |
|---|---|---|
| OTMga qabul voronkasi | Voronka | Nomzodlar → BMBAda ro'yxatdan o'tganlar → testda qatnashganlar → qabul qilinganlar |

**3-blok. So'rovnoma (anketa) natijalari**

| Diagramma | Turi | Mazmuni |
|---|---|---|
| Anketa to'ldirilishi | Progress / ustunli | Qismlar bo'yicha to'ldirilgan anketalar ulushi |
| Qiziqqan kasb yo'nalishlari | Ustunli (gorizontal) | 14-savol bo'yicha taqsimot |
| Kelgusi rejalar | Doiraviy | 15-savol bo'yicha taqsimot |
| OTM tayyorgarligi ehtiyoji | Ustunli | 17- va 18-savollar bo'yicha fanlar kesimida askarlar soni |
| Ta'lim darajasi va sertifikatlar | Ustunli (yig'ma) | Ta'lim turlari, til/kasb/fan sertifikatiga egalar, sovrindorlar |

**Umumiy ko'rsatkichlar paneli:** jami askarlar, guruhlar soni, yaqinlashayotgan nazorat muddatlari.

**Filtrlar:** davr, okrug, qism, kasb/fan, yillik sikl.

**Hisobotlar:**

- kurs yakuni hisoboti (HKTB uchun);
- OTMga qabul natijalari (HKTB uchun);
- vazirlik miqyosidagi yillik umumlashma.

Eksport: **XLSX, PDF**. Rasmiy blanklar mavjud emas, shuning uchun hisobot shakllari ishlab chiquvchi tomonidan yagona uslubda (sarlavha, davr, vakolat doirasi, tuzilgan sana va tuzuvchi F.I.Sh. bilan) loyihalanadi va 1-bosqichda (loyihalash) buyurtmachi bilan kelishiladi.

### M13. Muddatlar nazorati va bildirishnomalar

- Algoritmdagi har bir muddat tizimda nazorat nuqtasi sifatida saqlanadi (masalan: 1 fevral — biriktirish takliflari; 20 mart — kun tartibi; 30 mart — xatlov; 1 aprel — qo'shma qaror; 1 may — kurslar boshlanishi).
- Muddatdan oldin mas'ul rollarga eslatma, muddat o'tganda yuqori turuvchi darajaga xabar.
- Bildirishnomalar **faqat tizim ichida** (bildirishnomalar markazi va dashboarddagi ogohlantirishlar). SMS, Telegram va boshqa tashqi kanallar ishlatilmaydi.

### M14. Administrirlash va audit

- Foydalanuvchilar, rollar va vakolat doiralarini boshqarish.
- Dinamik ma'lumotnomalarni (fanlar, kasblar, sertifikat turlari) boshqarish.
- **Audit jurnali:** kim, qachon, qaysi yozuvni ko'rdi/o'zgartirdi (eski va yangi qiymatlari bilan); JShShIR bo'yicha so'rovlar ham jurnallanadi.
- Integratsiya jurnali: har bir so'rovning vaqti, holati, xatolari.
- Yillik siklni ochish/yopish (yangi chaqiruv yilini boshlash).
- Tizim sozlamalari: ma'lumotlarni saqlash muddati, anketa tanlov rejimlari, nazorat muddatlari.

---

## 7. Integratsiya talablari

### 7.1. Asosiy tamoyil — JShShIR bo'yicha so'rov + tizimda to'ldirish

1. **Manba tizimdan olish.** Xodim JShShIRni kiritadi, tizim manba tizimdan askar haqidagi mavjud ma'lumotlarni oladi.
2. **Tizimda to'ldirish.** Manba tizimda bo'lmagan ma'lumotlar (sertifikatlar, yutuqlar, PDF nusxalar va boshqalar) tizimning o'zida qo'shiladi.
3. **Manba belgisi.** Har bir maydon qaysi yo'l bilan kelgani saqlanadi.
4. **Nizolarni hal qilish.** Qayta so'rovda manba tizimdagi ma'lumot farq qilsa, avtomatik ustiga yozilmaydi — xodimga tasdiqlash uchun ko'rsatiladi.
5. **Barqarorlik.** Manba tizim ishlamay qolsa, asosiy tizim ishlashda davom etadi, ma'lumot qo'lda kiritiladi.

### 7.2. Integratsiya qilinadigan tizimlar

| № | Tizim / tashkilot | Yo'nalish | Ma'lumotlar | Usul |
|---|---|---|---|---|
| 1 | Manba tizim (chaqiriluvchilar bazasi) | Kiruvchi | JShShIR bo'yicha askarning shaxsiy ma'lumotlari, ma'lumoti va mavjud hujjatlari | API — JShShIR bo'yicha so'rov. Tizim nomi va API spetsifikatsiyasi askar ma'lumotlarini kiritish moduli (M2, anketa) ishlab chiqilayotganda taqdim etiladi. Ungacha anketa to'liq qo'lda kiritish rejimida ishlab chiqiladi |
| 2 | BMBA elektron platformasi | Kiruvchi | Ro'yxatdan o'tganlik, imtiyozlar, test natijalari, qabul natijalari | **API** (JShShIR bo'yicha); zaxira — qo'lda kiritish |
| 3 | Bandlik bo'yicha idoralar va hokimliklar | Chiquvchi | Kasbga o'qitilgan askarlar ro'yxati | XLSX / PDF eksport |
| 4 | Davlat ma'lumotnomalari | Kiruvchi | Hududlar, ta'lim muassasalari reyestri | Boshlang'ich yuklash XLSX orqali, keyin administrator yuritadi |

> "E-tarbiya" platformasi bilan integratsiya talab qilinmaydi: OTM tayyorlov kurslari bo'yicha ma'lumotlar tizim dashboardida ko'rsatiladi (M12).

### 7.3. Texnik talablar

- Integratsiya logikasi alohida **adapter** qatlamida bo'ladi — manba tizim API o'zgarsa, faqat adapter o'zgartiriladi.
- JShShIR bo'yicha so'rov uchun javob kutish vaqti cheklanadi (timeout); javob bo'lmasa xodimga tushunarli xabar chiqadi va qo'lda kiritish taklif etiladi.
- API mavjud bo'lmagan manbalar uchun **XLSX/CSV shablon orqali ommaviy import** (validatsiya va xatolar hisoboti bilan) qo'llab-quvvatlanadi.
- Barcha so'rovlar va almashuvlar jurnallanadi.
- BMBA API spetsifikatsiyasi tegishli integratsiya ishlab chiqilayotganda olinadi.
- Tarmoq ulanishi va huquqiy asos masalalari buyurtmachi tomonidan hal qilinadi va ushbu TT doirasiga kirmaydi.

---

## 8. Ma'lumotlar modeli (asosiy obyektlar)

| Obyekt | Asosiy atributlar | Bog'lanishlar |
|---|---|---|
| **Askar** (Soldier) | JShShIR, F.I.Sh., pasport seriyasi va raqami, tug'ilgan sana, yashash manzili (hudud, tuman, MFY, ko'cha, uy, xonadon), qarindosh telefoni, chaqiruv va tugash sanasi, ta'lim ma'lumoti, xizmatgacha kasbi; har bir maydon uchun manba belgisi | Qism, Hujjatlar, Guruh a'zoligi, Anketa, OTM arizasi, Bandlik ro'yxati |
| **Hujjat** (Document) | Turi, darajasi, sanasi, raqami, PDF fayl, manba (integratsiya/qo'lda) | Askar |
| **Harbiy okrug / Qism** | Nomi, kodi, manzili | Ierarxiya: okrug → qism → bo'linma |
| **Ta'lim muassasasi** | Turi (texnikum / maktab / o'quv markazi), nomi, hududi | Biriktirish, O'qituvchilar |
| **Biriktirish** | Qism, muassasa, asos hujjati, holati, muddati | Shartnoma, Qo'shma reja |
| **Kasb** | Nomi, kodi, o'quv dasturi, soatlar hajmi, faolligi | Guruh |
| **Fan** (dinamik) | Nomi, kodi, tayyorlov dasturi, soatlar hajmi, faolligi, amal qilish yillari | Guruh |
| **Anketa** (so'rovnoma) | Yillik sikl, holati (qoralama / yakunlangan), to'ldirgan psixolog, sana, qiziqqan kasb yo'nalishi, kelgusi rejalar, imzolangan skan | Askar, Qism, Anketa javoblari |
| **Anketa javobi (OTM rejasi)** | Ustuvorlik (1–3), OTM, ta'lim yo'nalishi | Anketa |
| **Kasb yo'nalishi** | Nomi, tavsifi, faolligi; qism bo'yicha faollashtirish | Anketa, Qism, Kasb |
| **Guruh** | Turi (kasb / OTM), qism, muassasa, sanalar, sinf, guruh kattasi | Askarlar, O'qituvchilar, Jadval |
| **O'qituvchi** (ma'lumot yozuvi) | F.I.Sh., mutaxassisligi, tashkiloti, kirish ruxsati rekvizitlari va muddati | Guruhlar |
| **Mashg'ulot** | Sana, vaqt, mavzu, turi (nazariy/amaliy), holati, o'qituvchi keldi/kelmadi | Guruh |
| **Kurs natijasi** | Imtihon bahosi, holat, sertifikat rekvizitlari | Askar, Guruh |
| **OTM arizasi** | BMBA holati, imtiyozlar, test bali, OTM, yo'nalish, onlayn o'qish, zaxiraga bo'shatish | Askar |
| **Bandlik ro'yxati** | Hudud, qabul qiluvchi idora, tayyorlangan sana, fayl | Askarlar |
| **Nazorat muddati** | Nomi, sana, mas'ul rol, holat | Yillik sikl |
| **Bildirishnoma** | Qabul qiluvchi, matn, turi, o'qilganligi | Foydalanuvchi |
| **Yillik sikl** | Chaqiruv yili, holati (ochiq/yopiq) | Barcha operatsion obyektlar |

---

## 9. Nofunksional talablar

### 9.1. Unumdorlik va masshtablanuvchanlik

- Tizim respublika miqyosidagi yillik chaqiruv hajmiga mo'ljallanadi va ma'lumotlar hajmi ortganda gorizontal kengaytirilishi mumkin bo'lishi kerak (aniq hajm yuklama sinovi bosqichida buyurtmachi bilan belgilanadi).
- Oddiy sahifalarning javob vaqti — 2 soniyadan oshmasligi; dashboard diagrammalari — 3 soniyagacha (agregatlar oldindan hisoblanadi yoki keshlanadi); og'ir hisobotlar fon rejimida tayyorlanadi.

### 9.2. Ishonchlilik

- Mavjudlik: ish vaqtida kamida 99%.
- Ma'lumotlar bazasining kunlik zaxira nusxasi, kamida 30 kun saqlanadi; zaxiradan tiklash tartibi sinovdan o'tkaziladi.
- Rejali texnik ishlar ish vaqtidan tashqarida.

### 9.3. Foydalanish qulayligi

- Interfeys tili: **o'zbek tili (lotin yozuvi)**. Barcha matnlar alohida lug'at fayllarida saqlanadi (kodga yozilmaydi).
- **Moslashuvchan (responsive) dizayn:** guruh kattasi mashg'ulot ma'lumotlarini, psixolog so'rovnomani telefon yoki planshetdan kiritishi kerak.
- Past tezlikli internet sharoitida ishlash (yengil sahifalar). Oflayn rejim talab qilinmaydi; aloqa uzilganda foydalanuvchiga ma'lumot saqlanmagani haqida aniq xabar ko'rsatiladi.
- Minimal texnik bilim talab qiladigan oddiy interfeys; har bir rol uchun qisqa foydalanuvchi qo'llanmasi.

### 9.4. Kengaytiriluvchanlik

- Yangi kasb, fan, sertifikat turi yoki diagramma qo'shish kodni o'zgartirmasdan (ma'lumotnomalar orqali) yoki minimal o'zgartirish bilan amalga oshiriladi.
- Muddatlar va kun tartibi sozlamalar orqali boshqariladi.

---

## 10. Axborot xavfsizligi talablari

Tizim harbiy xizmatchilarning shaxsga doir ma'lumotlarini va harbiy qismlar haqidagi ma'lumotlarni qayta ishlaydi, shuning uchun xavfsizlik talablari ustuvor hisoblanadi.

1. **Qonunchilikka muvofiqlik:** "Shaxsga doir ma'lumotlar to'g'risida"gi O'zbekiston Respublikasi Qonuni va Mudofaa vazirligining ichki axborot xavfsizligi talablariga rioya etish. Maxfiylik darajasi va infratuzilma buyurtmachi tomonidan belgilanadi; tizim ularga moslashtiriladigan bo'lishi kerak.
2. **Joylashuv:** server va ma'lumotlar O'zbekiston hududida, buyurtmachi belgilagan infratuzilmada joylashadi.
3. **Foydalanuvchilar doirasi:** tizimga faqat MV tizimidagi xodimlar kiradi; tashqi tashkilotlar uchun kirish nuqtasi yaratilmaydi — bu hujum yuzasini sezilarli qisqartiradi.
4. **Autentifikatsiya:** murakkab parol siyosati, **ikki bosqichli autentifikatsiya** (kamida respublika va okrug darajasidagi rollar uchun), sessiya vaqtini cheklash, bir necha muvaffaqiyatsiz urinishdan keyin bloklash.
5. **Avtorizatsiya:** RBAC + ierarxik vakolat doirasi — foydalanuvchi faqat o'z qismi/okrugi ma'lumotlarini ko'radi; tekshiruv server tomonida amalga oshiriladi. Dashboard agregatlari ham vakolat doirasiga qarab cheklanadi.
6. **JShShIR so'rovlarini nazorat qilish:** manba tizimga so'rov yuborish huquqi faqat vakolatli rollarda; har bir so'rov jurnallanadi; ommaviy so'rovlarga cheklov qo'yiladi.
7. **Shifrlash:** barcha trafik TLS orqali; PDF hujjatlar va maxfiy maydonlar saqlashda shifrlanadi.
8. **Audit:** barcha ko'rish, o'zgartirish, eksport va chop etish harakatlari jurnallanadi; audit jurnalini o'zgartirib bo'lmaydi.
9. **Eksportni cheklash:** eksport faqat vakolatli rollarga; tashqi idoralar uchun eksportda faqat zarur minimal maydonlar bo'ladi.
10. **Ma'lumotlarni saqlash muddati — sozlanadigan:** administrator xizmatni tugatgan askarlar ma'lumotlarini saqlash muddatini sozlamalarda belgilaydi. Muddat tugaganda yozuvlar avtomatik arxivlanadi yoki anonimlashtiriladi (statistik hisobotlar uchun agregatlar saqlanib qoladi); amal bajarilishidan oldin administratorga ogohlantirish beriladi va amal audit jurnaliga yoziladi.
11. **Xavfsizlik sinovi:** ishga tushirishdan oldin zaifliklarga tekshiruv (penetratsion test) o'tkaziladi.

---

## 11. Texnologik stek va arxitektura (tavsiya)

### 11.1. Arxitektura

Modulli monolit arxitekturasi tavsiya etiladi: bitta ilova ichida modullar (M1–M14) aniq chegaralar bilan ajratiladi.

```
[Brauzer / Mobil brauzer]  (faqat MV xodimlari)
          │ HTTPS
          ▼
[Frontend: React SPA]  ──►  [Backend API (REST)]
                                 │
        ┌──────────────┬─────────┴──────────┬──────────────────┐
        ▼              ▼                    ▼                  ▼
 [PostgreSQL]   [Fayl ombori (PDF)]  [Navbat / fon ishlari]  [Integratsiya adapteri]
                                     (hisobotlar, agregat,   (JShShIR bo'yicha so'rov
                                      bildirishnomalar)        → manba tizim)
```

### 11.2. Tavsiya etilgan texnologiyalar

| Qatlam | Tavsiya | Izoh |
|---|---|---|
| Frontend | React + TypeScript, Vite | Feature-based struktura, loyihadagi `.claude/` clean code qoidalariga muvofiq |
| UI | Tayyor komponent kutubxonasi (masalan, shadcn/ui yoki Ant Design) | Jadvallar, formalar va dashboardlar ko'p |
| Diagrammalar | Recharts yoki Apache ECharts | Chiziqli, ustunli, doiraviy va voronka diagrammalari, drill-down |
| Server holati | TanStack Query | Ma'lumotlarni keshlash va sinxronizatsiya |
| Backend | ⚠ Jamoa tajribasiga qarab tanlanadi (masalan, Laravel, NestJS yoki Django) | REST API, OpenAPI hujjati bilan |
| Ma'lumotlar bazasi | PostgreSQL | Ierarxik ma'lumotlar, agregat hisobotlar va audit uchun mos |
| Fayl ombori | S3-mos ombor (masalan, MinIO) — lokal joylashtirilgan | PDF hujjatlar uchun |
| Navbat / kesh | Redis | Fon ishlari, dashboard agregatlari keshi |
| Joylashtirish | Docker konteynerlari | Buyurtmachi infratuzilmasida |

### 11.3. Frontend struktura namunasi

```
src/
├── app/                 # provider'lar, router, global sozlamalar
├── pages/               # sahifalar (Dashboard, SoldierProfile, GroupPage...)
├── features/
│   ├── soldiers/        # M2 — yig'ma jild, JShShIR bo'yicha qidiruv
│   ├── dictionaries/    # M1 — fanlar, kasblar va boshqa ma'lumotnomalar
│   ├── assignments/     # M3 — biriktirishlar
│   ├── surveys/         # M5 — so'rovnomalar
│   ├── groups/          # M6 — guruhlar va o'qituvchilar
│   ├── schedule/        # M7 — jadval
│   ├── results/         # M9 — natijalar va sertifikatlar
│   ├── admissions/      # M10 — OTMga qabul
│   ├── employment/      # M11 — bandlik ro'yxatlari
│   ├── dashboard/       # M12 — yo'nalishlar bo'yicha diagrammalar
│   ├── reports/         # M12 — hisobotlar va eksport
│   └── notifications/   # M13 — muddatlar va bildirishnomalar
├── components/ui/       # umumiy UI primitivlar
├── hooks/  lib/  utils/  types/
```

---

## 12. Ishlab chiqish bosqichlari

| Bosqich | Mazmuni | Natija |
|---|---|---|
| **0. Tahlil** | Jarayonlarni buyurtmachi bilan tasdiqlash, 14-bo'limdagi savollarga javob olish | Tasdiqlangan TT |
| **1. Loyihalash** | Ma'lumotlar modeli, API spetsifikatsiyasi, interfeys maketlari (asosiy ekranlar va dashboard) | Arxitektura hujjati, maketlar |
| **2. MVP** | M1 (dinamik ma'lumotnomalar), elektron anketa va M2 (avval to'liq qo'lda kiritish; API spetsifikatsiyasi olingach — JShShIR orqali avtomatik to'ldirish), M5 (anketa natijalari), M6 (guruhlar va o'qituvchilar), M7 (jadval), M12 (dashboard — ikki yo'nalish diagrammalari), M14 (rollar, audit) | Bir necha pilot qismda ishlay oladigan tizim |
| **3. Pilot** | 1–2 harbiy okrug qismlarida sinov, foydalanuvchilar fikrini yig'ish | Pilot hisoboti, tuzatishlar |
| **4. To'liq funksional** | M3, M5, M9, M10, M11, M13; hisobotlar va eksportlar | To'liq tizim |
| **5. Qo'shimcha integratsiyalar** | BMBA bilan API integratsiyasi | Test natijalari va qabul natijalarini avtomatik olish |
| **6. Joriy etish** | Respublika bo'ylab ishga tushirish, foydalanuvchilarni o'qitish, qo'llanmalar | Ekspluatatsiyaga topshirish |

### 12.1. Kalendar reja (qo'lda to'ldiriladi)

**MVP topshirilish sanasi:** «____» ______________ 20___-yil

| Bosqich | Boshlanish sanasi | Tugash sanasi | Mas'ul | Izoh |
|---|---|---|---|---|
| 0. Tahlil | «___» ________ 20__ | «___» ________ 20__ | | |
| 1. Loyihalash | «___» ________ 20__ | «___» ________ 20__ | | |
| 2. MVP | «___» ________ 20__ | «___» ________ 20__ | | MVP topshirilish sanasidan kechikmasligi kerak |
| 3. Pilot | «___» ________ 20__ | «___» ________ 20__ | | |
| 4. To'liq funksional | «___» ________ 20__ | «___» ________ 20__ | | |
| 5. Qo'shimcha integratsiyalar | «___» ________ 20__ | «___» ________ 20__ | | |
| 6. Joriy etish | «___» ________ 20__ | «___» ________ 20__ | | |

> **MVP muddati:** MVP uchun buyurtmachi tomonidan qat'iy muddat belgilangan (yuqoridagi maydonga kiritiladi). Shu sababli MVP tarkibiga faqat 2-bosqichdagi modullar kiritiladi, qolganlari keyingi bosqichlarga qoldiriladi. Muddat yillik siklning 3-bosqichidan (1 may — kurslar boshlanishi) keyin tushsa, anketa va yig'ma jild (aprel — so'rovnoma davri) birinchi navbatda tayyorlanadi.

---

## 13. Sinov va qabul qilish tartibi

### 13.1. Sinov turlari

- Modul (unit) va integratsion testlar — avtomatlashtirilgan.
- Har bir rol uchun ssenariy bo'yicha funksional sinov.
- Manba tizim bilan integratsiya sinovi (topilgan, topilmagan JShShIR, manba tizim ishlamayotgan holat).
- Yuklama sinovi (dashboard agregatlari).
- Xavfsizlik sinovi (10-bo'lim, 11-band).
- Pilot qismlarda foydalanuvchilar tomonidan qabul sinovi.

### 13.2. Qabul qilish mezonlari (asosiy)

1. JShShIR kiritilganda askar ma'lumotlari manba tizimdan avtomatik olinadi, yetishmagan maydonlarni qo'lda to'ldirish mumkin, har bir maydonning manbasi ko'rinadi.
2. Manba tizim javob bermaganda ham askarni qo'lda ro'yxatga olish mumkin.
3. Dashboardda okrug → qism darajasiga chuqurlashish (drill-down) ishlaydi va vakolat doirasidan tashqari ma'lumot ko'rinmaydi.
4. Administrator yangi fan qo'shganda u darhol guruh yaratish, so'rovnoma va hisobotlarda ishlatiladi.
5. Psixolog elektron anketani askar bilan birgalikda o'z profilidan to'ldira oladi; anketa amaldagi qog'oz anketaning barcha bandlarini qamrab oladi; V bo'lim faqat "oliy ta'limga kirish" tanlanganda ochiladi va majburiy bo'ladi; natijalar dashboardning "So'rovnoma natijalari" blokida avtomatik aks etadi.
6. Qism uchun faollashtirilgan kasb yo'nalishlari o'zgartirilganda anketada darhol yangi ro'yxat ko'rinadi, oldin to'ldirilgan anketalar javoblari saqlanib qoladi.
7. Yakunlangan anketa qog'oz shakliga mos PDF ko'rinishida chop etiladi.
8. O'qituvchilar tizimga kirmasdan, faqat ma'lumot yozuvi sifatida (F.I.Sh., mutaxassisligi, tashkiloti) yuritiladi.
9. Barcha o'zgarishlar va JShShIR so'rovlari audit jurnalida aks etadi.
10. Algoritmdagi barcha nazorat muddatlari bo'yicha tizim ichidagi eslatmalar ishlaydi.
11. Bandlik ro'yxati askarning yashash hududi bo'yicha to'g'ri guruhlanib, XLSX/PDF shaklida eksport qilinadi.

### 13.3. Topshiriladigan hujjatlar

- Dastur kodi va joylashtirish ko'rsatmalari.
- API hujjati (OpenAPI).
- Har bir rol uchun foydalanuvchi qo'llanmasi.
- Administrator qo'llanmasi (zaxira nusxa, tiklash, yangilash, ma'lumotnomalarni boshqarish).

---

## 14. Qabul qilingan qarorlar va qolgan ochiq masalalar

### 14.1. Buyurtmachi bilan kelishilgan qarorlar

| Masala | Qaror |
|---|---|
| Askar ma'lumotlari | JShShIR orqali manba tizimdan, qolgani anketa orqali qo'lda; API spetsifikatsiyasi M2 ishlab chiqilayotganda beriladi |
| Tashqi tashkilot xodimlari | Tizimga kirmaydi; o'qituvchilar faqat ma'lumot yozuvi |
| Fanlar ro'yxati | Dinamik |
| So'rovnoma | Psixolog o'z profilidan askar bilan birgalikda to'ldiradi |
| Bildirishnomalar | Faqat tizim ichida |
| Anketa 15-savoli | Tanlov rejimi sozlanadigan |
| Anketa 16-savoli | Erkin matn |
| BMBA | API orqali integratsiya |
| Tarmoq, huquqiy asos, maxfiylik darajasi, infratuzilma | Buyurtmachi hal qiladi, TT doirasidan tashqarida |
| Interfeys tili | O'zbek (lotin) |
| Oflayn rejim | Talab qilinmaydi |
| Ma'lumotlarni saqlash muddati | Sozlanadigan |
| Hisobot blanklari | Mavjud emas — ishlab chiquvchi loyihalaydi, buyurtmachi bilan kelishiladi |
| MVP muddati | Qat'iy muddat mavjud; sana va kalendar reja 12.1-bandda qo'lda to'ldiriladi |

### 14.2. Qolgan ochiq masalalar

1. Backend texnologiyasi — ishlab chiquvchi jamoa tomonidan 1-bosqichda (loyihalash) tanlanadi.
