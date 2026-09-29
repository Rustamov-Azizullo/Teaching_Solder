package uz.askar.education.groups;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** O'qituvchi — faqat ma'lumot yozuvi (F.I.Sh., mutaxassislik, muassasa); tizim foydalanuvchisi emas (TT M6). */
@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    /** Mutaxassisliklar nomlari vergul bilan (`specialties` nusxasi); ro'yxatlarda ko'rsatish uchun. */
    private String specialty;

    /** Mutaxassisliklar: kasb yoki fan ma'lumotnomasidan (bir nechta bo'lishi mumkin). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "teacher_specialties", joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "dictionary_item_id"))
    private Set<uz.askar.education.dictionaries.DictionaryItem> specialties = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id")
    private EducationInstitution institution;

}
