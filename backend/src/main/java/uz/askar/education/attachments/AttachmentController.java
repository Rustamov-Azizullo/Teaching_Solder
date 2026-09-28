package uz.askar.education.attachments;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.askar.education.attachments.AttachmentService.AttachmentDto;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Fayllar (PDF nusxalar)", description = "Shifrlangan holda saqlanadi")
public class AttachmentController {

    private final AttachmentService service;

    @GetMapping
    public List<AttachmentDto> list(@RequestParam AttachmentOwnerType ownerType, @RequestParam Long ownerId) {
        return service.list(ownerType, ownerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.ATTACHMENT_WRITE)
    public AttachmentDto upload(@RequestParam AttachmentOwnerType ownerType, @RequestParam Long ownerId,
                                @RequestParam(required = false) String kind, @RequestParam("file") MultipartFile file) {
        return service.upload(ownerType, ownerId, kind, file);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        var file = service.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(file.content());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.ATTACHMENT_WRITE)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
