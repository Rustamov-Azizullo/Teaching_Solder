package uz.askar.education.attachments;

/**
 * Har bir egasi turi (askar, biriktirish, ...) o'z vakolat tekshiruvini beradi (OCP: yangi tur — yangi bean).
 * {@link #requireAccess} egasi mavjud emas yoki vakolat doirasida bo'lmasa istisno tashlaydi.
 */
public interface AttachmentOwnerResolver {

    AttachmentOwnerType type();

    void requireAccess(Long ownerId);
}
