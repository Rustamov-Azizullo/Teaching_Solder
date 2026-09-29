package uz.askar.education.common;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ApiError body = new ApiError(java.time.Instant.now(), 400, "Bad Request",
                "Majburiy bandlarni to'liq va to'g'ri to'ldiring", fieldErrors, null);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "So'rov tanasi noto'g'ri yoki to'liq emas");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> handleForbidden(RuntimeException ex) {
        return build(HttpStatus.FORBIDDEN, "Bu amal uchun sizning vakolatingiz yetarli emas");
    }

    @ExceptionHandler(uz.askar.education.auth.InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(uz.askar.education.auth.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage(), ex.code()));
    }

    @ExceptionHandler(uz.askar.education.soldiers.integration.SourceSystemUnavailableException.class)
    public ResponseEntity<ApiError> handleSourceUnavailable(RuntimeException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Manba tizim javob bermadi. Ma'lumotlarni qo'lda kiritishingiz mumkin");
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getConstraintViolations().iterator().next().getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Ma'lumotlar yaxlitligi buzildi", ex);
        return build(HttpStatus.CONFLICT, "Ma'lumot allaqachon mavjud yoki bog'liq yozuvlar bor");
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequests(TooManyRequestsException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.multipart.support.MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> handleBadParameter(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "So'rov parametrlari noto'g'ri yoki yetishmayapti");
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(Exception ex) {
        return build(HttpStatus.NOT_FOUND, "So'ralgan manzil topilmadi");
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(Exception ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Bu manzil uchun so'rov usuli qo'llab-quvvatlanmaydi");
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaType(Exception ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "So'rov formati qo'llab-quvvatlanmaydi");
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadTooLarge(Exception ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Fayl hajmi ruxsat etilganidan katta");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Kutilmagan xatolik", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Serverda xatolik yuz berdi");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), status.getReasonPhrase(), message));
    }
}
