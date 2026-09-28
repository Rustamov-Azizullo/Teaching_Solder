package uz.askar.education.integrations;

import java.util.Collection;
import java.util.Map;

/** Kasbiy ta'lim agentligi bilan aloqa abstraksiyasi (API spetsifikatsiyasi olingach implementatsiya almashtiriladi). */
public interface KtaClient {

    /** @return JShShIR → KTA tizimidagi sertifikat raqami (sertifikati bo'lmaganlar kiritilmaydi) */
    Map<String, String> certificatesByPinfl(Collection<String> pinfls);
}
