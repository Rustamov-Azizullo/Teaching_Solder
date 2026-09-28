package uz.askar.education.organization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilitaryDistrictRepository extends JpaRepository<MilitaryDistrict, Long> {

    List<MilitaryDistrict> findAllByOrderByNameAsc();
}
