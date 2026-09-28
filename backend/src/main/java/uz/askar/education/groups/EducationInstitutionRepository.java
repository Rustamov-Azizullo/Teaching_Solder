package uz.askar.education.groups;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationInstitutionRepository extends JpaRepository<EducationInstitution, Long> {

    List<EducationInstitution> findAllByOrderByNameAsc();
}
