package uz.askar.education.export;

import java.util.List;

/** Hisobot jadvali: sarlavha, davr/vakolat izohi, ustunlar va qatorlar (yagona uslubdagi hisobot shakli). */
public record TableData(String title, List<String> meta, List<String> headers, List<List<String>> rows) {
}
