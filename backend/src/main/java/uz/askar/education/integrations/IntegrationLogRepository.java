package uz.askar.education.integrations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> {

    Page<IntegrationLog> findAllByOrderByAtDesc(Pageable pageable);
}
