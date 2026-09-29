package uz.askar.education.locations;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.locations.LocationService.LocationDto;
import uz.askar.education.locations.LocationService.LocationRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Hududlar", description = "Foydalanuvchilarni biriktirish uchun hududlar daraxti (vazirlik / okrug / qism)")
public class LocationController {

    private final LocationService locationService;

    /** Tekis ro'yxat: daraxtni {@code parentId} orqali quriladi. Okrug admini faqat o'z okrugini ko'radi. */
    @GetMapping
    @PreAuthorize(Access.USER_ADMIN_ROLES)
    public List<LocationDto> list() {
        return locationService.listInScope();
    }

    /** Filtrlar uchun: vakolat doirasidagi hududlar daraxti (vazirlik / okrug / qism), har qanday kirgan foydalanuvchiga. */
    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public List<LocationDto> tree() {
        return locationService.listInScope();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.LOCATION_MANAGE)
    public LocationDto create(@Valid @RequestBody LocationRequest request) {
        return locationService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.LOCATION_MANAGE)
    public LocationDto update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.LOCATION_MANAGE)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
