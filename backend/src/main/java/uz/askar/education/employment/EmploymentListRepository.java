package uz.askar.education.employment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentListRepository extends JpaRepository<EmploymentList, Long> {

    List<EmploymentList> findTop100ByOrderByGeneratedAtDesc();
}
