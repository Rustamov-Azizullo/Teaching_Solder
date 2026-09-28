package uz.askar.education.assignments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.attachments.AttachmentOwnerResolver;
import uz.askar.education.attachments.AttachmentOwnerType;

@Component
@RequiredArgsConstructor
public class AssignmentAttachmentResolver implements AttachmentOwnerResolver {

    private final AssignmentService service;

    @Override
    public AttachmentOwnerType type() {
        return AttachmentOwnerType.ASSIGNMENT;
    }

    @Override
    @Transactional(readOnly = true)
    public void requireAccess(Long ownerId) {
        service.find(ownerId);
    }
}
