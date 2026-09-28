package uz.askar.education.notifications;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.security.Role;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int PAGE_SIZE = 50;

    public record NotificationDto(Long id, String title, String body, String link, boolean read,
                                  LocalDateTime createdAt) {
    }

    private final NotificationRepository notifications;
    private final AppUserRepository users;
    private final CurrentUser currentUser;
    private final PermissionEvaluatorService permissions;

    @Transactional(propagation = Propagation.REQUIRED)
    public void notifyRoles(Collection<Role> roles, String title, String body, String link) {
        users.findByRoleInAndActiveTrue(roles).forEach(user -> create(user, title, body, link));
    }

    /** Qismga biriktirilgan va berilgan ruxsatga ega faol foydalanuvchilarga xabar. */
    @Transactional(propagation = Propagation.REQUIRED)
    public void notifyUnitPermissionHolders(Long unitId, Permission permission, String title, String body, String link) {
        users.findByLocationMilitaryUnitIdAndActiveTrue(unitId).stream()
                .filter(user -> permissions.holds(user.getId(), user.getRole(), permission))
                .forEach(user -> create(user, title, body, link));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void notifyUser(Long userId, String title, String body, String link) {
        users.findById(userId).ifPresent(user -> create(user, title, body, link));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> mine() {
        return notifications.findByUserIdOrderByCreatedAtDesc(currentUser.id(), PageRequest.of(0, PAGE_SIZE)).stream()
                .map(n -> new NotificationDto(n.getId(), n.getTitle(), n.getBody(), n.getLink(), n.isRead(),
                        n.getCreatedAt())).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notifications.countByUserIdAndReadFalse(currentUser.id());
    }

    @Transactional
    public void markRead(Long id) {
        Notification notification = notifications.findById(id).orElseThrow(() -> new NotFoundException("Bildirishnoma topilmadi"));
        if (!notification.getUser().getId().equals(currentUser.id())) {
            throw new NotFoundException("Bildirishnoma topilmadi");
        }
        notification.setRead(true);
    }

    @Transactional
    public void markAllRead() {
        notifications.markAllRead(currentUser.id());
    }

    private void create(AppUser user, String title, String body, String link) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body.length() > 500 ? body.substring(0, 500) : body);
        notification.setLink(link);
        notification.setCreatedAt(LocalDateTime.now());
        notifications.save(notification);
    }
}
