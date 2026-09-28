package uz.askar.education.soldiers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.askar.education.attachments.AttachmentOwnerResolver;
import uz.askar.education.attachments.AttachmentOwnerType;

@Component
@RequiredArgsConstructor
public class SoldierAttachmentResolver implements AttachmentOwnerResolver {

    private final SoldierService soldierService;

    @Override
    public AttachmentOwnerType type() {
        return AttachmentOwnerType.SOLDIER;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void requireAccess(Long ownerId) {
        soldierService.findInScope(ownerId);
    }
}
