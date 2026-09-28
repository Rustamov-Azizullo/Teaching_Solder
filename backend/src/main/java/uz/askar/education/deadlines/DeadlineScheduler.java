package uz.askar.education.deadlines;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineScheduler implements CommandLineRunner {

    private final DeadlineService service;

    /** Joriy yil uchun standart muddatlar mavjud bo'lmasa, ishga tushishda yaratiladi. */
    @Override
    public void run(String... args) {
        service.seedStandard(LocalDate.now().getYear());
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void dailyCheck() {
        int sent = service.process(LocalDate.now());
        log.info("Muddatlar nazorati: {} ta bildirishnoma", sent);
    }
}
