package uz.askar.education.organization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerritorialDistrictRepository extends JpaRepository<TerritorialDistrict, Long> {

    List<TerritorialDistrict> findByRegionIdOrderByNameAsc(Long regionId);
}
