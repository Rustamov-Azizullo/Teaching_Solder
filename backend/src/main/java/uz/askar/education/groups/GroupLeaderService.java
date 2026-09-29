package uz.askar.education.groups;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.groups.GroupDtos.GroupLeaderInput;

/** Guruh kattasi guruh bilan birga kiritiladi (alohida sahifasi yo'q): yaratish, yangilash va guruhdan ajratish. */
@Service
@RequiredArgsConstructor
public class GroupLeaderService {

    private final GroupLeaderRepository leaders;
    private final StudyGroupRepository groups;

    /**
     * Guruhning kattasini belgilaydi. Shu JShShIR bilan katta shu qismda mavjud bo'lsa — o'sha ishlatiladi;
     * aks holda guruhning hozirgi kattasi yangilanadi yoki yangisi yaratiladi.
     */
    @Transactional
    public GroupLeader assign(StudyGroup group, GroupLeaderInput input) {
        GroupLeader current = group.getLeader();
        GroupLeader target = leaders.findByPinfl(input.pinfl()).map(existing -> {
            if (!existing.getMilitaryUnit().getId().equals(group.getMilitaryUnit().getId())) {
                throw new BusinessRuleException("Bu JShShIR bilan guruh kattasi boshqa harbiy qismda ro'yxatga olingan");
            }
            return existing;
        }).orElseGet(() -> current != null ? current : newLeader(group));
        target.setFullName(input.fullName().trim());
        target.setPinfl(input.pinfl());
        target.setMilitaryRank(input.militaryRank());
        target.setPhone(input.phone());
        GroupLeader saved = leaders.save(target);
        group.setLeader(saved);
        if (current != null && !current.getId().equals(saved.getId())) {
            deleteIfUnused(current);
        }
        return saved;
    }

    @Transactional
    public void detach(StudyGroup group) {
        GroupLeader current = group.getLeader();
        group.setLeader(null);
        if (current != null) {
            deleteIfUnused(current);
        }
    }

    private GroupLeader newLeader(StudyGroup group) {
        GroupLeader leader = new GroupLeader();
        leader.setMilitaryUnit(group.getMilitaryUnit());
        return leader;
    }

    private void deleteIfUnused(GroupLeader leader) {
        groups.flush();
        if (groups.findByLeaderId(leader.getId()).isEmpty()) {
            leaders.delete(leader);
        }
    }
}
