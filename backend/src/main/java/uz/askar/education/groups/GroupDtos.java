package uz.askar.education.groups;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import uz.askar.education.organization.OrganizationDtos.NamedRef;

import uz.askar.education.soldiers.SoldierDtos;

public final class GroupDtos {

    private GroupDtos() {
    }

    /** {@code professionIds}/{@code subjectIds} — muassasada o'qitiladigan kasblar va fanlar ({@code null} — o'zgarmaydi). */
    public record InstitutionRequest(@NotNull InstitutionType type, @NotBlank @Size(max = 255) String name,
                                     Long regionId, Set<Long> professionIds, Set<Long> subjectIds) {
    }

    public record InstitutionDto(Long id, InstitutionType type, String name, List<Long> professionIds,
                                 List<Long> subjectIds) {

        static InstitutionDto from(EducationInstitution institution) {
            return new InstitutionDto(institution.getId(), institution.getType(), institution.getName(),
                    idsOf(institution, uz.askar.education.dictionaries.DictionaryType.PROFESSION),
                    idsOf(institution, uz.askar.education.dictionaries.DictionaryType.SUBJECT));
        }

        private static List<Long> idsOf(EducationInstitution institution, uz.askar.education.dictionaries.DictionaryType type) {
            return institution.getSpecialties().stream().filter(item -> item.getType() == type)
                    .map(item -> item.getId()).sorted().toList();
        }
    }

    /** Muassasaning harbiy qismga biriktirilishi (shartnoma). */
    public record ContractDto(Long institutionId, String institutionName, InstitutionType institutionType,
                              Long unitId, String unitName, String districtName) {
    }

    public record ContractRequest(@NotNull Long institutionId, @NotNull Long unitId) {
    }

    public record TeacherRequest(
            @NotBlank @Size(max = 200) String fullName,
            @NotEmpty Set<Long> specialtyIds,
            @NotNull Long institutionId) {
    }

    public record TeacherDto(Long id, String fullName, String specialty, List<Long> specialtyIds,
                             Long institutionId,
                             String institutionName) {
    }

    public record GroupRequest(
            @NotBlank @Size(max = 160) String name,
            @NotNull GroupType type,
            @NotNull Long militaryUnitId,
            Long institutionId,
            Long professionId,
            Set<Long> subjectIds,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @Size(max = 80) String classroom,
            @Valid GroupLeaderInput leader) {
    }

    public record MembersRequest(Set<Long> soldierIds) {
    }

    public record TeachersRequest(Set<Long> teacherIds) {
    }

    public record MemberDto(Long id, String pinfl, String fullName) {
    }

    public record GroupSummary(Long id, String name, GroupType type, Long militaryUnitId, String militaryUnitName,
                               String professionName, List<String> subjectNames, LocalDate startDate,
                               LocalDate endDate, int memberCount, String leaderName) {
    }

    public record GroupDto(Long id, String name, GroupType type, Long militaryUnitId, String militaryUnitName,
                           NamedRef institution, NamedRef profession, List<NamedRef> subjects, LocalDate startDate,
                           LocalDate endDate, String classroom, LeaderDto leader,
                           List<MemberDto> members, List<TeacherDto> teachers) {
    }

    /** Guruh shakllantirilayotganda kiritiladigan guruh kattasi; harbiy qism guruhdan aniqlanadi. */
    public record GroupLeaderInput(@NotBlank @Size(max = 200) String fullName,
                                   @NotBlank @Pattern(regexp = SoldierDtos.PINFL_REGEX,
                                           message = "JShShIR 14 ta raqamdan iborat bo'lishi kerak") String pinfl,
                                   @Size(max = 100) String militaryRank, @Size(max = 40) String phone) {
    }

    public record LeaderDto(Long id, String fullName, String pinfl, String militaryRank, String phone) {

        static LeaderDto from(GroupLeader leader) {
            return new LeaderDto(leader.getId(), leader.getFullName(), leader.getPinfl(), leader.getMilitaryRank(),
                    leader.getPhone());
        }
    }
}
