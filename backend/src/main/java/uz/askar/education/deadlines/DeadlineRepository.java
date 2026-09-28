package uz.askar.education.deadlines;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    List<Deadline> findByCycleYearOrderByDeadlineDateAsc(int cycleYear);

    List<Deadline> findByStatus(DeadlineStatus status);

    long countByCycleYear(int cycleYear);
}
