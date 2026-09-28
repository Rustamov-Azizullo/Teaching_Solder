package uz.askar.education.cycles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;

/** Yillik siklni ochish/yopish (TT M14). Yopiq siklda yangi askar va guruh yaratib bo'lmaydi. */
@Service
@RequiredArgsConstructor
public class CycleService {

    public record CycleDto(int year, String status, LocalDateTime openedAt, LocalDateTime closedAt) {
    }

    private final CycleRepository cycles;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<CycleDto> list() {
        return cycles.findAllByOrderByYearDesc().stream().map(this::toDto).toList();
    }

    /** Joriy yil sikli yopilgan bo'lsa, istisno tashlaydi; mavjud bo'lmasa, avtomatik ochadi. */
    @Transactional
    public void requireCurrentOpen() {
        int year = LocalDate.now().getYear();
        Cycle cycle = cycles.findById(year).orElseGet(() -> open(year));
        if (!cycle.isOpen()) {
            throw new BusinessRuleException(year + "-yil sikli yopilgan. Administrator yangi sikl ochishi kerak");
        }
    }

    @Transactional
    public CycleDto openNew(int year) {
        if (cycles.existsById(year)) {
            throw new BusinessRuleException(year + "-yil sikli allaqachon mavjud");
        }
        audit.record("OPEN_CYCLE", "Cycle", year, "sikl ochildi");
        return toDto(open(year));
    }

    @Transactional
    public CycleDto close(int year) {
        Cycle cycle = cycles.findById(year).orElseThrow(() -> new NotFoundException("Sikl topilmadi"));
        cycle.setStatus("CLOSED");
        cycle.setClosedAt(LocalDateTime.now());
        audit.record("CLOSE_CYCLE", "Cycle", year, "sikl yopildi");
        return toDto(cycle);
    }

    private Cycle open(int year) {
        Cycle cycle = new Cycle();
        cycle.setYear(year);
        cycle.setStatus("OPEN");
        cycle.setOpenedAt(LocalDateTime.now());
        return cycles.save(cycle);
    }

    private CycleDto toDto(Cycle c) {
        return new CycleDto(c.getYear(), c.getStatus(), c.getOpenedAt(), c.getClosedAt());
    }
}
