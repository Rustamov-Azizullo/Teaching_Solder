package uz.askar.education.groups;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupLeaderRepository extends JpaRepository<GroupLeader, Long> {

    Optional<GroupLeader> findByPinfl(String pinfl);
}
