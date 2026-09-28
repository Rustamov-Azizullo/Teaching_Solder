package uz.askar.education.groups;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.groups.GroupDtos.GroupDto;
import uz.askar.education.groups.GroupDtos.GroupLeaderOption;
import uz.askar.education.groups.GroupDtos.GroupRequest;
import uz.askar.education.groups.GroupDtos.GroupSummary;
import uz.askar.education.groups.GroupDtos.LeaderRequest;
import uz.askar.education.groups.GroupDtos.MembersRequest;
import uz.askar.education.groups.GroupDtos.TeachersRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Guruhlar", description = "M6: kasb kursi va OTM tayyorlov guruhlari")
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/groups")
    @PreAuthorize(Access.GROUP_READ)
    public List<GroupSummary> list(@RequestParam(required = false) GroupType type) {
        return groupService.list(type);
    }

    @GetMapping("/groups/{id}")
    @PreAuthorize(Access.GROUP_READ)
    public GroupDto get(@PathVariable Long id) {
        return groupService.get(id);
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.GROUP_WRITE)
    public GroupDto create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request);
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize(Access.GROUP_WRITE)
    public GroupDto update(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return groupService.update(id, request);
    }

    @PutMapping("/groups/{id}/leader")
    @PreAuthorize(Access.GROUP_LEADER_ASSIGN)
    public GroupDto assignLeader(@PathVariable Long id, @Valid @RequestBody LeaderRequest request) {
        return groupService.assignLeader(id, request);
    }

    @PutMapping("/groups/{id}/facility")
    @PreAuthorize(Access.GROUP_LEADER_ASSIGN)
    public GroupDto assignFacility(@PathVariable Long id, @RequestBody FacilityRequest request) {
        return groupService.assignFacility(id, request.facilityId());
    }

    public record FacilityRequest(@jakarta.validation.constraints.NotNull Long facilityId) {
    }

    @PutMapping("/groups/{id}/members")
    @PreAuthorize(Access.GROUP_WRITE)
    public GroupDto replaceMembers(@PathVariable Long id, @RequestBody MembersRequest request) {
        return groupService.replaceMembers(id, request.soldierIds());
    }

    @PutMapping("/groups/{id}/teachers")
    @PreAuthorize(Access.GROUP_WRITE)
    public GroupDto replaceTeachers(@PathVariable Long id, @RequestBody TeachersRequest request) {
        return groupService.replaceTeachers(id, request.teacherIds());
    }

    @GetMapping("/military-units/{unitId}/group-leaders")
    @PreAuthorize(Access.GROUP_LEADER_ASSIGN)
    public List<GroupLeaderOption> leaderOptions(@PathVariable Long unitId) {
        return groupService.leaderOptions(unitId);
    }
}
