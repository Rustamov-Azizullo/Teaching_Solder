export const dashboardLabels = {
  other: 'Boshqalar',
  title: 'Umumiy ko\'rinish',
  series: { soldiers: 'Askarlar' },
  kpi: {
    total: 'Jami askarlar', vocational: "Kasb kurslarida o'qiyapti", otm: 'OTMga tayyorlanmoqda',
    certified: 'Sertifikati borlar', higher: "Oliy ma'lumotli", higherIncomplete: 'Tugallanmagan oliy',
  },
  directions: "Askarlar yo'nalishlar bo'yicha",
  directionNames: { vocational: 'Kasb kursida', otm: 'OTMga tayyorlanmoqda', unassigned: 'Kursga biriktirilmagan' },
  professions: 'Kasblar kesimida askarlar',
  profession: 'Kasb', soldiers: 'Askarlar', district: 'Okrug', unit: 'Harbiy qism', subdivision: "Bo'linma",
  districtBreakdown: {
    titles: {
      district: { composition: 'Okruglar: askarlar tarkibi', programs: 'Okruglar: kasb va muassasalar', bubble: 'Okruglar: solishtirish' },
      unit: { composition: 'Harbiy qismlar: askarlar tarkibi', programs: 'Harbiy qismlar: kasb va muassasalar', bubble: 'Harbiy qismlar: solishtirish' },
      subdivision: { composition: "Bo'linmalar: askarlar tarkibi", programs: "Bo'linmalar: kasb va muassasalar", bubble: "Bo'linmalar: solishtirish" },
    },
    vocational: 'Kasbga', otm: 'OTM tayyorlovga', unassigned: 'Biriktirilmagan',
    professions: 'Kasblar soni', institutions: 'Muassasalar soni',
    bubbleAxisX: 'Kasblar soni', bubbleAxisY: 'Muassasalar soni', bubbleSize: 'Kasbdagi askarlar',
    programsHeading: 'Kasb — muassasa', soldiers: 'askar', total: 'Jami',
  },
  regions: "Viloyatlar bo'yicha harbiy xizmatchilar",
} as const;
