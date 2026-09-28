package uz.askar.education.locations;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.locations.LocationService.LocationDto;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Hududlar", description = "Foydalanuvchilarni biriktirish uchun hududlar daraxti (respublika / okrug / qism)")
public class LocationController {

    private final LocationService locationService;

    /** Tekis ro'yxat: daraxtni {@code parentId} orqali quriladi. Okrug admini faqat o'z okrugini ko'radi. */
    @GetMapping
    @PreAuthorize(Access.USER_ADMIN_ROLES)
    public List<LocationDto> list() {
        return locationService.listInScope();
    }
}
