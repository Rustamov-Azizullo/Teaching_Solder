package uz.askar.education.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.ForbiddenException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.groups.GroupService;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.schedule.LessonDtos.CancelRequest;
import uz.askar.education.schedule.LessonDtos.GenerateLessonsRequest;
import uz.askar.education.schedule.LessonDtos.LessonDto;
import uz.askar.education.schedule.LessonDtos.LessonRequest;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.settings.SettingsService;

@Service
@RequiredArgsConstructor
public class LessonService {

    private static final String FALLBACK_START = "15:00";
    private static final String FALLBACK_END = "17:25";
    private static final String FALLBACK_HOURS = "3";

    private final LessonRepository lessons;
    private final GroupService groupService;
    private final SettingsService settings;
    private final CurrentUser currentUser;
    private final PermissionEvaluatorService permissions;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<LessonDto> listForGroup(Long groupId, LocalDate from, LocalDate to) {
        groupService.findInScope(groupId);
        return lessons.findByGroupIdAndLessonDateBetweenOrderByLessonDateAscStartTimeAsc(groupId, from, to)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LessonDto> listForDate(LocalDate date) {
        AccessScope scope = currentUser.scope();
        return lessons.findByDateInScope(date, scope.districtFilter(), scope.unitFilter(),
                        groupService.leaderRestriction()).stream()
                .map(this::toDto).toList();
    }

    @Transactional
    public LessonDto create(Long groupId, LessonRequest request) {
        StudyGroup group = groupService.findInScope(groupId);
        requireWithinGroupPeriod(group, request.lessonDate());
        Lesson lesson = new Lesson();
        lesson.setGroup(group);
        apply(lesson, request);
        Lesson saved = lessons.save(lesson);
        audit.record("CREATE", "Lesson", saved.getId(), "guruh=" + group.getName() + ", sana=" + saved.getLessonDate());
        return toDto(saved);
    }

    /** Dushanba–juma kunlari uchun standart vaqt (15:00–17:25) bilan mashg'ulotlarni yaratadi. */
    @Transactional
    public List<LessonDto> generate(Long groupId, GenerateLessonsRequest request) {
        StudyGroup group = groupService.findInScope(groupId);
        LocalDate from = max(request.from(), group.getStartDate());
        LocalDate to = min(request.to(), group.getEndDate());
        List<Lesson> created = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            if (isWeekend(day) || lessons.existsByGroupIdAndLessonDate(groupId, day)) {
                continue;
            }
            Lesson lesson = new Lesson();
            lesson.setGroup(group);
            apply(lesson, new LessonRequest(day, null, null, null, null, request.kind(), null));
            created.add(lesson);
        }
        List<LessonDto> result = lessons.saveAll(created).stream().map(this::toDto).toList();
        audit.record("GENERATE", "Lesson", groupId, "yaratilgan mashg'ulotlar: " + result.size());
        return result;
    }

    @Transactional
    public LessonDto update(Long lessonId, LessonRequest request) {
        Lesson lesson = findInScope(lessonId);
        requireWithinGroupPeriod(lesson.getGroup(), request.lessonDate());
        apply(lesson, request);
        audit.record("UPDATE", "Lesson", lessonId, "sana=" + lesson.getLessonDate());
        return toDto(lesson);
    }

    @Transactional
    public LessonDto cancel(Long lessonId, CancelRequest request) {
        Lesson lesson = findInScope(lessonId);
        lesson.setStatus(LessonStatus.CANCELLED);
        lesson.setChangeReason(request.reason());
        audit.record("CANCEL", "Lesson", lessonId, request.reason());
        return toDto(lesson);
    }

    /** Vakolat doirasida mashg'ulotni topish. */
    public Lesson findInScope(Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(() -> new NotFoundException("Mashg'ulot topilmadi"));
        groupService.findInScope(lesson.getGroup().getId());
        return lesson;
    }

    public LessonDto toDto(Lesson l) {
        return new LessonDto(l.getId(), l.getGroup().getId(), l.getGroup().getName(), l.getGroup().getType(),
                l.getLessonDate(), l.getStartTime(), l.getEndTime(), l.getAcademicHours(), l.getTopic(), l.getKind(),
                l.getStatus(), l.getChangeReason(), l.getTeacherPresent());
    }

    private void apply(Lesson lesson, LessonRequest request) {
        LocalTime defaultStart = LocalTime.parse(settings.get(SettingsService.LESSON_DEFAULT_START, FALLBACK_START));
        LocalTime defaultEnd = LocalTime.parse(settings.get(SettingsService.LESSON_DEFAULT_END, FALLBACK_END));
        int defaultHours = Integer.parseInt(settings.get(SettingsService.LESSON_DEFAULT_HOURS, FALLBACK_HOURS));

        LocalTime start = request.startTime() != null ? request.startTime() : defaultStart;
        LocalTime end = request.endTime() != null ? request.endTime() : defaultEnd;
        int hours = request.academicHours() != null ? request.academicHours() : defaultHours;
        if (!end.isAfter(start)) {
            throw new BusinessRuleException("Tugash vaqti boshlanish vaqtidan keyin bo'lishi kerak");
        }
        boolean isStandard = start.equals(defaultStart) && end.equals(defaultEnd) && hours == defaultHours;
        if (!isStandard) {
            requireTimeOverridePermission();
            if (request.changeReason() == null || request.changeReason().isBlank()) {
                throw new BusinessRuleException("Standart vaqtdan farqli bo'lsa, sababi ko'rsatilishi shart");
            }
        }
        lesson.setLessonDate(request.lessonDate());
        lesson.setStartTime(start);
        lesson.setEndTime(end);
        lesson.setAcademicHours(hours);
        lesson.setTopic(request.topic());
        lesson.setKind(request.kind());
        lesson.setChangeReason(isStandard ? null : request.changeReason());
    }

    /**
     * Standart vaqtni o'zgartirish jadval tuzish ({@code SCHEDULE_WRITE}) huquqidan torroq: alohida
     * {@code SCHEDULE_TIME_OVERRIDE} ruxsati talab qilinadi (masalan, qism operatori jadval tuza oladi,
     * lekin standart vaqtni faqat qo'mondon o'zgartiradi).
     */
    private void requireTimeOverridePermission() {
        if (!permissions.currentUserHas(Permission.SCHEDULE_TIME_OVERRIDE)) {
            throw new ForbiddenException("Standart vaqtni o'zgartirish uchun vakolat yetarli emas");
        }
    }

    private void requireWithinGroupPeriod(StudyGroup group, LocalDate date) {
        if (date.isBefore(group.getStartDate()) || date.isAfter(group.getEndDate())) {
            throw new BusinessRuleException("Mashg'ulot sanasi guruh o'qish davridan tashqarida");
        }
    }

    private boolean isWeekend(LocalDate day) {
        return day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
