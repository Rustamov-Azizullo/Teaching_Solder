package uz.askar.education.notifications;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.notifications.NotificationService.NotificationDto;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Bildirishnomalar", description = "Faqat tizim ichida (M13)")
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public List<NotificationDto> mine() {
        return service.mine();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", service.unreadCount());
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        service.markRead(id);
    }

    @PostMapping("/read-all")
    public void markAllRead() {
        service.markAllRead();
    }
}
