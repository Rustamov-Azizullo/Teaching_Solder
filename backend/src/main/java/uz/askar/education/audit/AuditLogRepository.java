package uz.askar.education.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            select a from AuditLog a
            where lower(a.username) like lower(concat('%', :username, '%'))
              and lower(a.entity) like lower(concat('%', :entity, '%'))
            order by a.at desc
            """)
    Page<AuditLog> search(@Param("username") String username, @Param("entity") String entity, Pageable pageable);
}
