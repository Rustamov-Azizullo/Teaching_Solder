package uz.askar.education.reports;

/**
 * Haftalik hisobot ob'ekti: vakolat darajasiga mos kesim yoki umumiy ({@code view}), yoxud aniq bitta okrug, harbiy qism
 * yoki bo'linma. Tanlangan ob'ekt foydalanuvchining vakolat doirasida bo'lishi shart; bittadan ortiq tanlash mumkin emas.
 */
public record ReportTarget(ReportView view, Long districtId, Long unitId, Long subdivisionId) {

    public boolean isSelection() {
        return districtId != null || unitId != null || subdivisionId != null;
    }

    public boolean hasSingleSelection() {
        int selected = (districtId != null ? 1 : 0) + (unitId != null ? 1 : 0) + (subdivisionId != null ? 1 : 0);
        return selected <= 1;
    }
}
