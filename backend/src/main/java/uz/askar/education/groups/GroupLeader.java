package uz.askar.education.groups;

import jakarta.persistence.Entity;
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
import uz.askar.education.organization.MilitaryUnit;

/** Guruh kattasi: tizimga kirmaydigan ma'lumot yozuvi (o'qituvchi kabi); qism foydalanuvchisi yaratadi va guruhga biriktiradi. */
@Entity
@Table(name = "group_leaders")
@Getter
@Setter
@NoArgsConstructor
public class GroupLeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String pinfl;
    private String militaryRank;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;
}
