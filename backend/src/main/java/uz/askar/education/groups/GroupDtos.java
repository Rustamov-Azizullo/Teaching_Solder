package uz.askar.education.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import uz.askar.education.organization.OrganizationDtos.NamedRef;

public final class GroupDtos {

    private GroupDtos() {
    }

    public record InstitutionRequest(@NotNull InstitutionType type, @NotBlank @Size(max = 255) String name,
                                     Long regionId) {
    }

    public record InstitutionDto(Long id, InstitutionType type, String name) {

        static InstitutionDto from(EducationInstitution institution) {
            return new InstitutionDto(institution.getId(), institution.getType(), institution.getName());
        }
    }

    public record TeacherRequest(
            @NotBlank @Size(max = 200) String fullName,
            @NotBlank @Size(max = 255) String specialty,
            @NotNull Long institutionId,
            @NotNull Long militaryUnitId,
            @Size(max = 60) String accessOrderNo,
            LocalDate accessValidUntil) {
    }

    public record TeacherDto(Long id, String fullName, String specialty, Long institutionId, String institutionName,
                             Long militaryUnitId, String accessOrderNo, LocalDate accessValidUntil,
                             boolean accessExpiringSoon) {
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
            @Size(max = 80) String classroom) {
    }

    public record LeaderRequest(@NotNull Long userId, @NotBlank @Size(max = 60) String orderNo,
                                @NotNull LocalDate orderDate) {
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
                           LocalDate endDate, String classroom, NamedRef leader, String leaderOrderNo,
                           LocalDate leaderOrderDate, List<MemberDto> members, List<TeacherDto> teachers) {
    }

    public record GroupLeaderOption(Long id, String fullName) {
    }
}
