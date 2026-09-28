package uz.askar.education.soldiers;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.dictionaries.DictionaryItem;

@Entity
@Table(name = "soldier_certificates")
@Getter
@Setter
@NoArgsConstructor
public class SoldierCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soldier_id")
    private Soldier soldier;

    @Enumerated(EnumType.STRING)
    private CertificateKind kind;

    /** Til yoki fan (kind ga qarab); kasb sertifikati uchun {@code null}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_item_id")
    private DictionaryItem dictionaryItem;

    /** Kasb sertifikati uchun kasb/hunar nomi. */
    private String title;

    private String level;
}
