package uz.askar.education.soldiers;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoldierTransferRepository extends JpaRepository<SoldierTransfer, Long> {

    List<SoldierTransfer> findBySoldierIdOrderByTransferredAtDesc(Long soldierId);
}
