package uz.askar.education.cycles;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleRepository extends JpaRepository<Cycle, Integer> {

    List<Cycle> findAllByOrderByYearDesc();
}
