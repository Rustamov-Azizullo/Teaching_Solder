package uz.askar.education.dictionaries;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class DictionaryDtos {

    private DictionaryDtos() {
    }

    public record DictionaryItemDto(Long id, String type, String code, String name, String description,
                                    Integer hours, boolean active) {

        public static DictionaryItemDto from(DictionaryItem item) {
            return new DictionaryItemDto(item.getId(), item.getType().name(), item.getCode(), item.getName(),
                    item.getDescription(), item.getHours(), item.isActive());
        }
    }

    public record DictionaryItemRequest(
            @NotBlank @Size(max = 60) String code,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 500) String description,
            @Min(0) Integer hours,
            boolean active) {
    }

    public record DictionaryTypeDto(String code, String label) {
    }

    public record UnitDirectionsRequest(List<Long> directionIds) {
    }
}
