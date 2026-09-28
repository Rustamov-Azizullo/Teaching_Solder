package uz.askar.education.results;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.attachments.AttachmentOwnerResolver;
import uz.askar.education.attachments.AttachmentOwnerType;
import uz.askar.education.groups.GroupService;

/** Qaydnoma skani guruh natijalariga biriktiriladi (owner id = guruh id). */
@Component
@RequiredArgsConstructor
public class ResultAttachmentResolver implements AttachmentOwnerResolver {

    private final GroupService groupService;

    @Override
    public AttachmentOwnerType type() {
        return AttachmentOwnerType.RESULT;
    }

    @Override
    @Transactional(readOnly = true)
    public void requireAccess(Long ownerId) {
        groupService.findInScope(ownerId);
    }
}
