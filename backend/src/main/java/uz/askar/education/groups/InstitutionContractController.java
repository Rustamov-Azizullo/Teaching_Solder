package uz.askar.education.groups;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.groups.GroupDtos.ContractDto;
import uz.askar.education.groups.GroupDtos.ContractRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/institution-contracts")
@RequiredArgsConstructor
@Tag(name = "Muassasani qismga biriktirish", description = "Ta'lim muassasasi va harbiy qism o'rtasidagi shartnoma")
public class InstitutionContractController {

    private final InstitutionContractService service;

    @GetMapping
    public List<ContractDto> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.GROUP_WRITE)
    public ContractDto create(@Valid @RequestBody ContractRequest request) {
        return service.create(request);
    }

    /** Query parametrlari — o'zgartiriladigan (eski) juftlik, tana — yangi juftlik. */
    @PutMapping
    @PreAuthorize(Access.GROUP_WRITE)
    public ContractDto update(@RequestParam Long institutionId, @RequestParam Long unitId,
                              @Valid @RequestBody ContractRequest request) {
        return service.update(institutionId, unitId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.GROUP_WRITE)
    public void delete(@RequestParam Long institutionId, @RequestParam Long unitId) {
        service.delete(institutionId, unitId);
    }
}
