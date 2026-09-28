# `.claude/` — Clean React loyihasi uchun konfiguratsiya

Bu papka Claude Code'ga loyihada **feature-based arxitektura**, **SOLID** va
**dizayn patternlar** asosida toza (clean) kod yozishni o'rgatadi.

## Tuzilma

```
.claude/
├── CLAUDE.md                      # Loyiha bo'yicha asosiy yo'riqnoma (ildizdagi yagona .md, har sessiyada o'qiladi)
├── skills/                        # Faqat skill'lar — Claude avtomatik chaqiradi
│   ├── react-clean-architecture/  #   → fayl qayerga joylashishi, import chegaralari
│   │   └── SKILL.md
│   └── react-component-patterns/  #   → komponent/hook patternlari
│       └── SKILL.md
├── agents/                        # Faqat subagentlar (delegatsiya uchun)
│   ├── code-reviewer.md           #   → kodni clean-code + SOLID bo'yicha tekshiradi
│   ├── architecture-guardian.md   #   → fayl joylashuvi va import chegaralarini nazorat qiladi
│   ├── refactoring-specialist.md  #   → kodni patternga moslab refaktor qiladi
│   └── test-writer.md             #   → yonma-yon (colocated) testlar yozadi
└── rules/                         # Qolgan barcha fayllar (odam o'qiydigan qoidalar + shu README)
    ├── README.md                  #   → shu fayl
    ├── clean-code.md
    ├── solid-principles.md
    └── design-patterns.md
```

## Har bir qism nima qiladi

- **CLAUDE.md** — Claude Code har sessiya boshida o'qiydigan asosiy qoidalar:
  arxitektura xaritasi, import chegaralari, "tayyorlik" ro'yxati.
- **skills/** — Claude vazifaga qarab **o'zi** chaqiradigan ko'nikmalar. Masalan,
  yangi `.tsx` fayl yaratayotganda `react-clean-architecture` avtomatik ishga tushadi.
- **agents/** — alohida vazifalarni topshirsa bo'ladigan subagentlar. Masalan:
  *"kodni tekshir"* → `code-reviewer`, *"buni refaktor qil"* → `refactoring-specialist`.
- **rules/** — inson uchun mo'ljallangan qoidalar hujjatlari (o'zbek tilida, kod
  misollari ingliz tilida). Skill va agentlar shu qoidalarga tayanadi. Bularni
  talabalar/jamoa uchun o'quv materiali sifatida ham ishlatsa bo'ladi.

## Ishlatish

1. Bu `.claude/` papkasini React loyihangiz ildiziga qo'ying.
2. Loyihada Claude Code'ni ishga tushiring — u `CLAUDE.md` va skill'larni avtomatik
   o'qiydi.
3. Subagentlarni chaqirish: `code-reviewer bilan ushbu komponentni tekshir` kabi
   iltimoslar yozing (yoki Claude proaktiv chaqiradi).

## Moslashtirish

- Yangi feature turlari yoki papkalar qo'shsangiz — `CLAUDE.md` dagi "Directory
  map" va `react-clean-architecture` skill'ini yangilang.
- Test kutubxonangiz boshqa bo'lsa (masalan Jest) — `test-writer.md` dagi
  misollarni moslang.
- `rules/` hujjatlarini ingliz tiliga o'girishni istasangiz — so'rasangiz
  tayyorlab beraman.
