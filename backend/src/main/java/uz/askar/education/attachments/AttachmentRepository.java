package uz.askar.education.attachments;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(AttachmentOwnerType type, Long ownerId);
}
