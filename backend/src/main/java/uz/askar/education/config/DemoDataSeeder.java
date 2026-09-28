package uz.askar.education.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.groups.EducationInstitution;
import uz.askar.education.groups.EducationInstitutionRepository;
import uz.askar.education.groups.GroupType;
import uz.askar.education.groups.InstitutionType;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.groups.StudyGroupRepository;
import uz.askar.education.groups.Teacher;
import uz.askar.education.groups.TeacherRepository;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryDistrictRepository;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.Region;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.organization.TerritorialDistrict;
import uz.askar.education.organization.TerritorialDistrictRepository;
import uz.askar.education.schedule.Lesson;
import uz.askar.education.schedule.LessonKind;
import uz.askar.education.schedule.LessonRepository;
import uz.askar.education.schedule.LessonStatus;
import uz.askar.education.security.Role;
import uz.askar.education.soldiers.GeneralEducation;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierRepository;
import uz.askar.education.surveys.Questionnaire;
import uz.askar.education.surveys.QuestionnaireRepository;
import uz.askar.education.surveys.QuestionnaireStatus;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;

/**
 * Namunaviy (demo) ma'lumotlar: tizimni birinchi ishga tushirishda foydalanuvchilar, qismlar, askarlar,
 * guruhlar yaratadi. Productionda {@code SEED_DEMO_DATA=false} qilib o'chiriladi.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    static final String DEMO_PASSWORD = "Parol123!";
    private static final long RANDOM_SEED = 42;
    private static final int LESSON_HISTORY_DAYS = 21;
    private static final int COURSE_MONTHS = 5;

    private static final List<String> FIRST_NAMES = List.of("Jasur", "Sardor", "Otabek", "Bekzod", "Dilshod",
            "Sherzod", "Aziz", "Rustam", "Anvar", "Jahongir", "Mirzo", "Ulug'bek");
    private static final List<String> LAST_NAMES = List.of("Karimov", "Aliyev", "Rahimov", "Yusupov", "Tursunov",
            "Xolmatov", "Qodirov", "Ergashev", "Nazarov", "Saidov");

    private final SecurityProperties properties;
    private final AppUserRepository users;
    private final MilitaryDistrictRepository militaryDistricts;
    private final MilitaryUnitRepository militaryUnits;
    private final RegionRepository regions;
    private final TerritorialDistrictRepository districts;
    private final DictionaryItemRepository dictionaryItems;
    private final EducationInstitutionRepository institutions;
    private final TeacherRepository teachers;
    private final SoldierRepository soldiers;
    private final StudyGroupRepository groups;
    private final LessonRepository lessons;
    private final QuestionnaireRepository questionnaires;
    private final uz.askar.education.organization.SubdivisionRepository subdivisionRepository;
    private final uz.askar.education.assignments.AssignmentRepository assignmentRepository;
    private final uz.askar.education.results.CourseResultRepository courseResultRepository;
    private final uz.askar.education.admissions.AdmissionRepository admissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transaction;

    @Override
    public void run(String... args) {
        if (!properties.seedDemoData() || users.count() > 0) {
            return;
        }
        transaction.executeWithoutResult(status -> seed());
        log.warn("Demo ma'lumotlar yaratildi. Barcha demo foydalanuvchilar paroli: {}", DEMO_PASSWORD);
    }

    private void seed() {
        MilitaryDistrict districtOne = militaryDistrict("MO-1", "1-harbiy okrug (demo)");
        MilitaryDistrict districtTwo = militaryDistrict("MO-2", "2-harbiy okrug (demo)");
        MilitaryUnit unitOne = militaryUnit(districtOne, "Q-101", "101-harbiy qism (demo)");
        MilitaryUnit unitTwo = militaryUnit(districtOne, "Q-102", "102-harbiy qism (demo)");
        MilitaryUnit unitThree = militaryUnit(districtTwo, "Q-201", "201-harbiy qism (demo)");

        user("admin", "Tizim administratori", Role.SYSTEM_ADMIN, null, null);
        user("hktb", "HKTB xodimi", Role.HKTB, null, null);
        user("jtb", "JTB xodimi", Role.JTB, null, null);
        user("tmibb", "TMIBB xodimi", Role.TMIBB, null, null);
        user("okrug1", "1-okrug mas'uli", Role.DISTRICT_OFFICER, districtOne, null);
        user("qomondon1", "101-qism qo'mondoni", Role.UNIT_COMMANDER, null, unitOne);
        user("operator1", "101-qism operatori", Role.UNIT_OPERATOR, null, unitOne);
        user("jangovar1", "101-qism jangovar tayyorgarlik bo'limi", Role.COMBAT_TRAINING_DEPT, null, unitOne);
        user("tarbiya1", "101-qism tarbiyaviy ishlar bo'limi", Role.EDUCATION_DEPT, null, unitOne);
        AppUser leaderOne = user("katta1", "Guruh kattasi Nurmatov A.", Role.GROUP_LEADER, null, unitOne);
        AppUser leaderTwo = user("katta2", "Guruh kattasi Ismoilov B.", Role.GROUP_LEADER, null, unitOne);
        AppUser psychologist = user("psixolog1", "Harbiy psixolog Sobirova D.", Role.PSYCHOLOGIST, null, unitOne);
        AppUser leaderThree = user("katta3", "Guruh kattasi Hamidov S.", Role.GROUP_LEADER, null, unitThree);
        user("qomondon2", "201-qism qo'mondoni", Role.UNIT_COMMANDER, null, unitThree);

        Region tashkent = regions.findAllByOrderByNameAsc().stream()
                .filter(r -> r.getName().equals("Toshkent shahri")).findFirst().orElseThrow();
        TerritorialDistrict chilonzor = districts.findByRegionIdOrderByNameAsc(tashkent.getId()).get(0);
        DictionaryItem kinship = dictionaryItems.findByTypeOrderBySortOrderAscNameAsc(DictionaryType.KINSHIP).get(0);

        Random random = new Random(RANDOM_SEED);
        List<Soldier> unitOneSoldiers = soldiers(unitOne, tashkent, chilonzor, kinship, 24, 1, random);
        List<Soldier> unitThreeSoldiers = soldiers(unitThree, tashkent, chilonzor, kinship, 8, 2, random);

        EducationInstitution technicalSchool = institution(InstitutionType.TECHNICAL_SCHOOL, "1-son texnikum (demo)");
        EducationInstitution school = institution(InstitutionType.SCHOOL, "15-son maktab (demo)");
        teacher("Ergashev Karim", "Oshpazlik", technicalSchool, unitOne);
        teacher("Sodiqova Malika", "Matematika", school, unitOne);

        DictionaryItem cook = item(DictionaryType.PROFESSION, "COOK");
        StudyGroup vocational = group("Oshpazlik-1", GroupType.VOCATIONAL, unitOne, technicalSchool, cook, List.of(),
                unitOneSoldiers.subList(0, 12), leaderOne);
        StudyGroup otm = group("OTM tayyorlov-1", GroupType.OTM_PREP, unitOne, school, null,
                List.of(item(DictionaryType.SUBJECT, "MATH"), item(DictionaryType.SUBJECT, "MOTHER_TONGUE")),
                unitOneSoldiers.subList(12, 24), leaderTwo);
        StudyGroup other = group("Dasturlash-1", GroupType.VOCATIONAL, unitThree, technicalSchool,
                item(DictionaryType.PROFESSION, "PROGRAMMER"), List.of(), unitThreeSoldiers, leaderThree);

        List.of(vocational, otm, other).forEach(group -> seedLessons(group, random));
        questionnaires(unitOneSoldiers, psychologist, random);
        seedExtras(unitOne, unitThree, unitOneSoldiers, technicalSchool, school, vocational, otm);
    }

    private void seedExtras(MilitaryUnit unitOne, MilitaryUnit unitThree, List<Soldier> unitOneSoldiers,
                            EducationInstitution technicalSchool, EducationInstitution school, StudyGroup vocational,
                            StudyGroup otm) {
        var battalion = subdivision(unitOne, "1-batalon", null);
        var company = subdivision(unitOne, "1-rota", battalion);
        var platoonOne = subdivision(unitOne, "1-vzvod", company);
        var platoonTwo = subdivision(unitOne, "2-vzvod", company);
        subdivision(unitOne, "2-batalon", null);
        for (int i = 0; i < unitOneSoldiers.size(); i++) {
            unitOneSoldiers.get(i).setSubdivision(i % 2 == 0 ? platoonOne : platoonTwo);
        }

        assignment(unitOne, technicalSchool, GroupType.VOCATIONAL, uz.askar.education.assignments.AssignmentStatus.APPROVED);
        assignment(unitOne, school, GroupType.OTM_PREP, uz.askar.education.assignments.AssignmentStatus.APPROVED);
        assignment(unitThree, technicalSchool, GroupType.VOCATIONAL, uz.askar.education.assignments.AssignmentStatus.PROPOSED);

        List<Soldier> members = vocational.getSoldiers().stream().sorted(java.util.Comparator.comparing(Soldier::getId)).toList();
        uz.askar.education.results.CourseStatus[] pattern = {
                uz.askar.education.results.CourseStatus.CERTIFIED, uz.askar.education.results.CourseStatus.CERTIFIED,
                uz.askar.education.results.CourseStatus.CERTIFIED, uz.askar.education.results.CourseStatus.CERTIFIED,
                uz.askar.education.results.CourseStatus.CERTIFIED, uz.askar.education.results.CourseStatus.CERTIFIED,
                uz.askar.education.results.CourseStatus.EXAM_PASSED, uz.askar.education.results.CourseStatus.EXAM_PASSED,
                uz.askar.education.results.CourseStatus.EXAM_PASSED, uz.askar.education.results.CourseStatus.STUDIED,
                uz.askar.education.results.CourseStatus.STUDIED, uz.askar.education.results.CourseStatus.DROPPED};
        for (int i = 0; i < members.size(); i++) {
            var result = new uz.askar.education.results.CourseResult();
            result.setGroup(vocational);
            result.setSoldier(members.get(i));
            result.setStatus(pattern[i % pattern.length]);
            if (result.getStatus() == uz.askar.education.results.CourseStatus.CERTIFIED) {
                result.setCertificateNo("SRT-2026-" + (100 + i));
                result.setCertificateDate(LocalDate.now().minusDays(3));
                result.setCertificateIssuer("1-son texnikum");
            }
            if (result.getStatus() == uz.askar.education.results.CourseStatus.DROPPED) {
                result.setDropReason("Sog'lig'i tufayli");
            }
            result.setExamGrade(result.getStatus() == uz.askar.education.results.CourseStatus.STUDIED ? null : 80 + i);
            result.setRecordedBy("operator1");
            result.setUpdatedAt(LocalDateTime.now());
            courseResultRepository.save(result);
            if (i < 4) {
                members.get(i).setServiceEndDate(LocalDate.now().plusDays(20));
            }
        }

        List<Soldier> candidates = otm.getSoldiers().stream().sorted(java.util.Comparator.comparing(Soldier::getId)).toList();
        for (int i = 0; i < Math.min(6, candidates.size()); i++) {
            var admission = new uz.askar.education.admissions.Admission();
            admission.setSoldier(candidates.get(i));
            admission.setCycleYear(LocalDate.now().getYear());
            admission.setBmbaRegistered(i < 5);
            admission.setBenefitsUploaded(i < 4);
            admission.setTestParticipated(i < 3);
            admission.setTestScore(i < 3 ? 130.0 + i * 8 : null);
            admission.setAdmitted(i < 2);
            admission.setUniversity(i < 2 ? "O'zbekiston Milliy universiteti" : null);
            admission.setStudyDirection(i < 2 ? "Amaliy matematika" : null);
            admission.setUpdatedAt(LocalDateTime.now());
            admission.setUpdatedBy("operator1");
            admissionRepository.save(admission);
        }
    }

    private uz.askar.education.organization.Subdivision subdivision(MilitaryUnit unit, String name,
                                                                    uz.askar.education.organization.Subdivision parent) {
        var subdivision = new uz.askar.education.organization.Subdivision();
        subdivision.setMilitaryUnit(unit);
        subdivision.setName(name);
        subdivision.setParent(parent);
        return subdivisionRepository.save(subdivision);
    }

    private void assignment(MilitaryUnit unit, EducationInstitution institution, GroupType direction,
                            uz.askar.education.assignments.AssignmentStatus status) {
        var assignment = new uz.askar.education.assignments.Assignment();
        assignment.setMilitaryUnit(unit);
        assignment.setInstitution(institution);
        assignment.setDirection(direction);
        assignment.setStatus(status);
        assignment.setProposedBy("okrug1");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setCycleYear(LocalDate.now().getYear());
        if (status == uz.askar.education.assignments.AssignmentStatus.APPROVED) {
            assignment.setBasisDocument("Qo'shma qaror № 12");
            assignment.setValidFrom(LocalDate.now().minusMonths(2));
            assignment.setValidTo(LocalDate.now().plusMonths(10));
            assignment.setContractNo("SH-2026/17");
            assignment.setContractDate(LocalDate.now().minusMonths(2));
            assignment.setDecidedBy("hktb");
        }
        assignmentRepository.save(assignment);
    }

    private void seedLessons(StudyGroup group, Random random) {
        LocalDate today = LocalDate.now();
        for (LocalDate day = today.minusDays(LESSON_HISTORY_DAYS); !day.isAfter(today); day = day.plusDays(1)) {
            if (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            Lesson lesson = new Lesson();
            lesson.setGroup(group);
            lesson.setLessonDate(day);
            lesson.setStartTime(LocalTime.of(15, 0));
            lesson.setEndTime(LocalTime.of(17, 25));
            lesson.setAcademicHours(3);
            lesson.setKind(random.nextBoolean() ? LessonKind.THEORY : LessonKind.PRACTICAL);
            lesson.setTopic("Mavzu " + day.getDayOfMonth());
            boolean leaveTodayOpen = day.equals(today) && group.getType() == GroupType.OTM_PREP;
            if (!leaveTodayOpen) {
                lesson.setStatus(LessonStatus.HELD);
                lesson.setTeacherPresent(true);
            }
            lessons.save(lesson);
        }
    }

    private void questionnaires(List<Soldier> unitSoldiers, AppUser psychologist, Random random) {
        List<DictionaryItem> directions = dictionaryItems
                .findByTypeAndActiveTrueOrderBySortOrderAscNameAsc(DictionaryType.PROFESSION_DIRECTION);
        List<DictionaryItem> plans = dictionaryItems
                .findByTypeAndActiveTrueOrderBySortOrderAscNameAsc(DictionaryType.FUTURE_PLAN);
        List<DictionaryItem> subjects = dictionaryItems
                .findByTypeAndActiveTrueOrderBySortOrderAscNameAsc(DictionaryType.SUBJECT);
        DictionaryItem otmPlan = plans.stream().filter(p -> p.getCode().equals("OTM_ADMISSION")).findFirst().orElseThrow();
        for (Soldier soldier : unitSoldiers.subList(0, 16)) {
            Questionnaire questionnaire = new Questionnaire();
            questionnaire.setSoldier(soldier);
            questionnaire.setCycleYear(LocalDate.now().getYear());
            questionnaire.setStatus(QuestionnaireStatus.FINALIZED);
            questionnaire.setPsychologist(psychologist);
            questionnaire.setFilledDate(LocalDate.now());
            questionnaire.setUpdatedAt(LocalDateTime.now());
            questionnaire.setInterestDirection(directions.get(random.nextInt(directions.size() - 1)));
            boolean wantsUniversity = random.nextInt(3) == 0;
            questionnaire.setFuturePlans(new HashSet<>(List.of(wantsUniversity ? otmPlan
                    : plans.get(random.nextInt(3) == 0 ? 0 : 1))));
            if (wantsUniversity) {
                questionnaire.setSpecialtySubjects(new HashSet<>(List.of(subjects.get(3 + random.nextInt(3)))));
                questionnaire.setMandatorySubjects(new HashSet<>(List.of(subjects.get(random.nextInt(3)))));
                var choice = new uz.askar.education.surveys.UniversityChoice();
                choice.setQuestionnaire(questionnaire);
                choice.setPriority(1);
                choice.setUniversity("Toshkent axborot texnologiyalari universiteti");
                choice.setStudyDirection("Dasturiy injiniring");
                questionnaire.getUniversityChoices().add(choice);
            }
            questionnaires.save(questionnaire);
        }
    }

    private List<Soldier> soldiers(MilitaryUnit unit, Region region, TerritorialDistrict district,
                                   DictionaryItem kinship, int count, int unitNumber, Random random) {
        List<Soldier> created = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Soldier soldier = new Soldier();
            soldier.setPinfl(String.format("3%d%02d%010d", unitNumber, i, 10_000_000L + i * 7919L));
            soldier.setFullName(LAST_NAMES.get(random.nextInt(LAST_NAMES.size())) + " "
                    + FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size())) + " o'g'li");
            soldier.setBirthDate(LocalDate.of(2004 + random.nextInt(3), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            soldier.setPassport("AB" + String.format("%07d", 1_000_000 + i * 131 + unitNumber));
            soldier.setPhone("+99890" + String.format("%07d", 1_000_000 + i * 37));
            soldier.setPhoneKinship(kinship);
            soldier.setRegion(region);
            soldier.setDistrict(district);
            soldier.setMahalla("Bunyodkor MFY");
            soldier.setStreet("Navoiy ko'chasi");
            soldier.setHouse(String.valueOf(1 + i));
            soldier.setMilitaryUnit(unit);
            soldier.setConscriptionDate(LocalDate.now().minusMonths(3));
            soldier.setServiceEndDate(LocalDate.now().plusMonths(9));
            soldier.setGeneralEducation(random.nextInt(4) == 0 ? GeneralEducation.LYCEUM : GeneralEducation.SCHOOL);
            soldier.setNoPriorOccupation(true);
            soldier.setCycleYear(LocalDate.now().getYear());
            soldier.setCreatedAt(LocalDateTime.now());
            soldier.setUpdatedAt(LocalDateTime.now());
            created.add(soldiers.save(soldier));
        }
        return created;
    }

    private StudyGroup group(String name, GroupType type, MilitaryUnit unit, EducationInstitution institution,
                             DictionaryItem profession, List<DictionaryItem> subjects, List<Soldier> members,
                             AppUser leader) {
        StudyGroup group = new StudyGroup();
        group.setName(name);
        group.setType(type);
        group.setMilitaryUnit(unit);
        group.setInstitution(institution);
        group.setProfession(profession);
        group.setSubjects(new HashSet<>(subjects));
        group.setSoldiers(new HashSet<>(members));
        group.setStartDate(LocalDate.now().minusDays(LESSON_HISTORY_DAYS + 7));
        group.setEndDate(group.getStartDate().plusMonths(COURSE_MONTHS));
        group.setClassroom("12-sinf");
        group.setLeader(leader);
        group.setLeaderOrderNo("B-" + name.hashCode() % 100);
        group.setLeaderOrderDate(group.getStartDate());
        group.setCycleYear(LocalDate.now().getYear());
        return groups.save(group);
    }

    private void teacher(String fullName, String specialty, EducationInstitution institution, MilitaryUnit unit) {
        Teacher teacher = new Teacher();
        teacher.setFullName(fullName);
        teacher.setSpecialty(specialty);
        teacher.setInstitution(institution);
        teacher.setMilitaryUnit(unit);
        teacher.setAccessOrderNo("K-17");
        teacher.setAccessValidUntil(LocalDate.now().plusMonths(6));
        teachers.save(teacher);
    }

    private EducationInstitution institution(InstitutionType type, String name) {
        EducationInstitution institution = new EducationInstitution();
        institution.setType(type);
        institution.setName(name);
        return institutions.save(institution);
    }

    private DictionaryItem item(DictionaryType type, String code) {
        return dictionaryItems.findByTypeOrderBySortOrderAscNameAsc(type).stream()
                .filter(item -> item.getCode().equals(code)).findFirst().orElseThrow();
    }

    private MilitaryDistrict militaryDistrict(String code, String name) {
        MilitaryDistrict district = new MilitaryDistrict();
        district.setCode(code);
        district.setName(name);
        return militaryDistricts.save(district);
    }

    private MilitaryUnit militaryUnit(MilitaryDistrict district, String code, String name) {
        MilitaryUnit unit = new MilitaryUnit();
        unit.setMilitaryDistrict(district);
        unit.setCode(code);
        unit.setName(name);
        return militaryUnits.save(unit);
    }

    private AppUser user(String username, String fullName, Role role, MilitaryDistrict district, MilitaryUnit unit) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setFullName(fullName);
        user.setRole(role);
        user.setMilitaryDistrict(district);
        user.setMilitaryUnit(unit);
        return users.save(user);
    }
}
