package uz.askar.education.soldiers;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Maydon qaysi yo'l bilan kelgani: integratsiya yoki qo'lda (kim va qachon kiritgani bilan). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class FieldSourceInfo {

    @Enumerated(EnumType.STRING)
    private FieldSource source;

    private String filledBy;
    private LocalDateTime filledAt;

    public FieldSourceInfo(FieldSource source, String filledBy, LocalDateTime filledAt) {
        this.source = source;
        this.filledBy = filledBy;
        this.filledAt = filledAt;
    }
}
