export const organizationLabels = {
  subdivisions: {
    title: "Bo'linmalar",
    subtitle: "Harbiy qism tarkibidagi ierarxik bo'linmalar (batalon → rota → vzvod ...)",
    filterTitle: 'Harbiy qismni tanlang',
    pickUnit: "Bo'linmalarni ko'rish uchun okrug va harbiy qismni tanlang",
    tableTitle: "Bo'linmalar ro'yxati",
    count: (n: number) => `Bo'linmalar: ${n}`,
    soldiers: (n: number) => `Askarlar: ${n}`,
    columns: { name: 'Nomi', children: "Ichki bo'linmalar", soldiers: 'Askarlar' },
    addRoot: "Bo'linma qo'shish",
    addChild: "Ichki bo'linma qo'shish",
    rename: "Bo'linma nomini o'zgartirish",
    namePlaceholder: "Bo'linma nomi",
    confirmDelete: "Bo'linma o'chirilsinmi?",
    deleted: "Bo'linma o'chirildi",
  },
} as const;
