package uz.askar.education.attachments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import uz.askar.education.config.StorageProperties;

/** Fayllar diskda AES-256-GCM bilan shifrlangan holda saqlanadi (TT 10-bo'lim, 7-band). */
@Component
public class EncryptedFileStore {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final Path root;
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public EncryptedFileStore(StorageProperties properties) throws IOException, GeneralSecurityException {
        this.root = Path.of(properties.dir()).toAbsolutePath();
        Files.createDirectories(root);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.key = new SecretKeySpec(digest, "AES");
    }

    public String save(byte[] content) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(content);
            String name = UUID.randomUUID().toString();
            Files.write(root.resolve(name), ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
            return name;
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Faylni saqlab bo'lmadi", ex);
        }
    }

    public byte[] read(String name) {
        try {
            byte[] stored = Files.readAllBytes(root.resolve(name).normalize());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, stored, 0, IV_BYTES));
            return cipher.doFinal(stored, IV_BYTES, stored.length - IV_BYTES);
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Faylni o'qib bo'lmadi", ex);
        }
    }

    public void delete(String name) {
        try {
            Files.deleteIfExists(root.resolve(name).normalize());
        } catch (IOException ex) {
            throw new IllegalStateException("Faylni o'chirib bo'lmadi", ex);
        }
    }
}
