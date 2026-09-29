package uz.askar.education.attachments;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.security.CurrentUser;

@Service
public class AttachmentService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Map<String, byte[]> FILE_SIGNATURES = Map.of(
            "application/pdf", new byte[] {'%', 'P', 'D', 'F'},
            "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[] {(byte) 0x89, 'P', 'N', 'G'});
    private static final Set<String> ALLOWED_TYPES = FILE_SIGNATURES.keySet();

    public record AttachmentDto(Long id, String kind, String fileName, String contentType, long sizeBytes,
                                String uploadedBy, LocalDateTime uploadedAt) {
    }

    public record Download(String fileName, String contentType, byte[] content) {
    }

    private final AttachmentRepository attachments;
    private final EncryptedFileStore store;
    private final Map<AttachmentOwnerType, AttachmentOwnerResolver> resolvers;
    private final CurrentUser currentUser;
    private final AuditService audit;

    public AttachmentService(AttachmentRepository attachments, EncryptedFileStore store,
                             List<AttachmentOwnerResolver> resolvers, CurrentUser currentUser, AuditService audit) {
        this.attachments = attachments;
        this.store = store;
        this.resolvers = resolvers.stream().collect(Collectors.toMap(AttachmentOwnerResolver::type, Function.identity()));
        this.currentUser = currentUser;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<AttachmentDto> list(AttachmentOwnerType type, Long ownerId) {
        resolver(type).requireAccess(ownerId);
        return attachments.findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(type, ownerId).stream()
                .map(this::toDto).toList();
    }

    @Transactional
    public AttachmentDto upload(AttachmentOwnerType type, Long ownerId, String kind, MultipartFile file) {
        resolver(type).requireAccess(ownerId);
        if (file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new BusinessRuleException("Fayl bo'sh yoki 10 MB dan katta");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessRuleException("Faqat PDF, JPG yoki PNG fayllar qabul qilinadi");
        }
        try {
            if (!matchesSignature(contentType, file.getBytes())) {
                throw new BusinessRuleException("Fayl mazmuni ko'rsatilgan turga mos kelmaydi");
            }
            Attachment attachment = new Attachment();
            attachment.setOwnerType(type);
            attachment.setOwnerId(ownerId);
            attachment.setKind(kind);
            attachment.setFileName(sanitize(file.getOriginalFilename()));
            attachment.setContentType(contentType);
            attachment.setSizeBytes(file.getSize());
            attachment.setStorageName(store.save(file.getBytes()));
            attachment.setUploadedBy(currentUser.username());
            attachment.setUploadedAt(LocalDateTime.now());
            Attachment saved = attachments.save(attachment);
            audit.record("UPLOAD", "Attachment", saved.getId(), type + "#" + ownerId + " " + saved.getFileName());
            return toDto(saved);
        } catch (IOException ex) {
            throw new BusinessRuleException("Faylni o'qib bo'lmadi");
        }
    }

    @Transactional(readOnly = true)
    public Download download(Long id) {
        Attachment attachment = find(id);
        audit.record("DOWNLOAD", "Attachment", id, attachment.getFileName());
        return new Download(attachment.getFileName(), attachment.getContentType(), store.read(attachment.getStorageName()));
    }

    @Transactional
    public void delete(Long id) {
        Attachment attachment = find(id);
        attachments.delete(attachment);
        attachments.flush();
        store.delete(attachment.getStorageName());
        audit.record("DELETE", "Attachment", id, attachment.getFileName());
    }

    private Attachment find(Long id) {
        Attachment attachment = attachments.findById(id).orElseThrow(() -> new NotFoundException("Fayl topilmadi"));
        resolver(attachment.getOwnerType()).requireAccess(attachment.getOwnerId());
        return attachment;
    }

    private AttachmentOwnerResolver resolver(AttachmentOwnerType type) {
        AttachmentOwnerResolver resolver = resolvers.get(type);
        if (resolver == null) {
            throw new BusinessRuleException("Bu obyekt turi uchun fayl biriktirib bo'lmaydi");
        }
        return resolver;
    }

    /** Mijoz yuborgan turga ishonmaslik uchun faylning boshlang'ich baytlari (imzo) tekshiriladi. */
    private static boolean matchesSignature(String contentType, byte[] content) {
        byte[] signature = FILE_SIGNATURES.get(contentType);
        if (signature == null || content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private String sanitize(String name) {
        String base = name == null ? "fayl" : name.replaceAll("[^\\p{L}\\p{N}._ -]", "_");
        return base.length() > 200 ? base.substring(base.length() - 200) : base;
    }

    private AttachmentDto toDto(Attachment a) {
        return new AttachmentDto(a.getId(), a.getKind(), a.getFileName(), a.getContentType(), a.getSizeBytes(),
                a.getUploadedBy(), a.getUploadedAt());
    }
}
