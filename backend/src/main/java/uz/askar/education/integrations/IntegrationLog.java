package uz.askar.education.integrations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "integration_logs")
@Getter
@Setter
@NoArgsConstructor
public class IntegrationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "at")
    private LocalDateTime at;

    @Column(name = "system_name")
    private String system;

    private String operation;
    private String reference;
    private boolean success;
    private String message;
    private long durationMs;
    private String actor;
}
