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
  profession: 'Kasb', soldiers: 'Askarlar', district: 'Okrug', unit: 'Harbiy qism',
  districtBreakdown: {
    compositionTitle: 'Okruglar: askarlar tarkibi',
    programsTitle: 'Okruglar: kasb va muassasalar',
    bubbleTitle: 'Okruglar: solishtirish',
    vocational: 'Kasbga', otm: 'OTM tayyorlovga', unassigned: 'Biriktirilmagan',
    professions: 'Kasblar soni', institutions: 'Muassasalar soni',
    bubbleAxisX: 'Kasblar soni', bubbleAxisY: 'Muassasalar soni', bubbleSize: 'Kasbdagi askarlar',
    programsHeading: 'Kasb — muassasa', soldiers: 'askar', total: 'Jami',
  },
  regions: "Viloyatlar bo'yicha harbiy xizmatchilar",
} as const;
