package uz.askar.education.integrations;

import java.util.Optional;

/** BMBA platformasi (JShShIR bo'yicha). Javob bermasa, xodim qo'lda kiritadi (zaxira). */
public interface BmbaClient {

    record BmbaStatus(boolean registered, boolean benefitsUploaded, Double testScore, boolean admitted,
                      String university, String direction) {
    }

    Optional<BmbaStatus> findByPinfl(String pinfl);
}
