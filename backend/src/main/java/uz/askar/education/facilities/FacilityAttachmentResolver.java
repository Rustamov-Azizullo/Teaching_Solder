package uz.askar.education.facilities;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.attachments.AttachmentOwnerResolver;
import uz.askar.education.attachments.AttachmentOwnerType;

@Component
@RequiredArgsConstructor
public class FacilityAttachmentResolver implements AttachmentOwnerResolver {

    private final FacilityService service;

    @Override
    public AttachmentOwnerType type() {
        return AttachmentOwnerType.FACILITY;
    }

    @Override
    @Transactional(readOnly = true)
    public void requireAccess(Long ownerId) {
        service.findInScope(ownerId);
    }
}
