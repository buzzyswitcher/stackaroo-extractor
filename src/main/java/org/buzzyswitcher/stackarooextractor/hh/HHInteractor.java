package org.buzzyswitcher.stackarooextractor.hh;

import org.buzzyswitcher.stackarooextractor.dao.entity.Area;
import org.buzzyswitcher.stackarooextractor.dao.entity.Employer;
import org.buzzyswitcher.stackarooextractor.dao.entity.Employment;
import org.buzzyswitcher.stackarooextractor.dao.entity.Experience;
import org.buzzyswitcher.stackarooextractor.dao.entity.KeySkill;
import org.buzzyswitcher.stackarooextractor.dao.entity.Language;
import org.buzzyswitcher.stackarooextractor.dao.entity.ProfessionalRole;
import org.buzzyswitcher.stackarooextractor.dao.entity.RecruitSystem;
import org.buzzyswitcher.stackarooextractor.dao.entity.Salary;
import org.buzzyswitcher.stackarooextractor.dao.entity.Schedule;
import org.buzzyswitcher.stackarooextractor.dao.entity.Vacancy;
import org.buzzyswitcher.stackarooextractor.dao.entity.nsi.Theme;
import org.buzzyswitcher.stackarooextractor.dao.entity.nsi.ThemeEnum;
import org.buzzyswitcher.stackarooextractor.dao.repo.AreaRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.EmployerRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.EmploymentRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.ExperienceRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.KeySkillRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.LanguageRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.ProfessionalRoleRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.RecruitSystemRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.ScheduleRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.VacancyRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.nsi.ThemeRepo;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyKeySkill;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyLanguage;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyProfessionalRole;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@EnableScheduling
public class HHInteractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HHInteractor.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final VacancyRepo vacancyRepo;
    private final KeySkillRepo keySkillRepo;
    private final RecruitSystemRepo recruitSystemRepo;
    private final EmployerRepo employerRepo;
    private final EmploymentRepo employmentRepo;
    private final ExperienceRepo experienceRepo;
    private final ProfessionalRoleRepo professionalRoleRepo;
    private final ScheduleRepo scheduleRepo;
    private final LanguageRepo languageRepo;
    private final AreaRepo areaRepo;
    private final ThemeRepo themeRepo;

    public HHInteractor(
            KeySkillRepo keySkillRepo,
            RecruitSystemRepo recruitSystemRepo,
            VacancyRepo vacancyRepo,
            EmployerRepo employerRepo,
            EmploymentRepo employmentRepo,
            ExperienceRepo experienceRepo,
            ProfessionalRoleRepo professionalRoleRepo,
            ScheduleRepo scheduleRepo,
            LanguageRepo languageRepo,
            AreaRepo areaRepo,
            ThemeRepo themeRepo) {
        this.keySkillRepo = keySkillRepo;
        this.recruitSystemRepo = recruitSystemRepo;
        this.vacancyRepo = vacancyRepo;
        this.employerRepo = employerRepo;
        this.employmentRepo = employmentRepo;
        this.experienceRepo = experienceRepo;
        this.professionalRoleRepo = professionalRoleRepo;
        this.scheduleRepo = scheduleRepo;
        this.languageRepo = languageRepo;
        this.areaRepo = areaRepo;
        this.themeRepo = themeRepo;
    }

    /**
     * Фильтрует извлеченные идентификаторы на предмет наличия в БД записи Vacancy с таким идентификаром.
     * В случае если такая вакансия есть, то к ней привязвается новая тема. Такая запись не попадет в итоговый сет из Id
     * @param unfilteredSet извлеченные идентификаторы вакансий
     * @param contextTheme текущая тема
     * @return сет фильтрованных идентификаторов
     */
    @Transactional
    public Set<String> filterVacancyIds(Set<String> unfilteredSet, ThemeEnum contextTheme) {
        Set<String> filteredIdSet = new HashSet<>();
        Theme theme = themeRepo.findFirstByTheme(contextTheme);
        LOGGER.info("Filter vacancy id set. Size of unfiltered set set: [{}]", unfilteredSet.size());
        for (String vacancyId : unfilteredSet) {
            if (vacancyRepo.existsBySystemId(vacancyId)) {
                LOGGER.debug("Vacancy - system_id: [{}] exists in database", vacancyId);
                if (!vacancyRepo.containingTheme(theme.getTheme().getText(), vacancyId)) {
                    LOGGER.debug("Theme [{}] was added to vacancy with system_id [{}]", theme.getTheme().getText(), vacancyId);
                    Vacancy vacancy = vacancyRepo.findFirstBySystemId(vacancyId);
                    vacancy.getThemes().add(theme);
                    theme.getVacancies().add(vacancy);
                    vacancyRepo.merge(vacancy);
                    themeRepo.merge(theme);
                }
            } else {
                LOGGER.debug("Vacancy - system_id: [{}] is new. Add to filtered set", vacancyId);
                filteredIdSet.add(vacancyId);
            }
        }
        LOGGER.info("Filtering successfully finished. Filtered set size is: [{}]", filteredIdSet.size());
        return filteredIdSet;
    }

    @Transactional
    public Set<Vacancy> convert(Set<HHVacancyResponse> responses, ThemeEnum themeEnum) {
        LOGGER.info("Start converting");
        RecruitSystem recruitSystem = recruitSystemRepo.findFirstByName("HEAD_HUNTER");
        Theme theme = themeRepo.findFirstByTheme(themeEnum);
        Set<Vacancy> vacancies = new HashSet<>();
        for (HHVacancyResponse response : responses) {
            LOGGER.info("Vacancy id - [{}]. START CONVERTING", response.getId());
            Vacancy vacancy = new Vacancy();

            vacancy.setRecruitSystem(recruitSystem);
            vacancy.setSystemId(response.getId());
            vacancy.setDescription(response.getDescription());
            vacancy.setCreatedAt(LocalDateTime.parse(response.getCreatedAt(), formatter));
            vacancy.setInitialCreatedAt(LocalDateTime.parse(response.getInitialCreatedAt(), formatter));
            vacancy.setPublishedAt(LocalDateTime.parse(response.getPublishedAt(), formatter));
            vacancy.setSyncAt(LocalDateTime.now());

            vacancy.getThemes().add(theme);
            theme.getVacancies().add(vacancy);

            initKeySkills(recruitSystem, vacancy, response);
            initSalary(vacancy, response);
            initEmployer(recruitSystem, vacancy, response);
            initArea(recruitSystem, vacancy, response);
            initEmployment(recruitSystem, vacancy, response);
            initExperience(recruitSystem, vacancy, response);
            initProfessionalRole(recruitSystem, vacancy, response);
            initSchedule(recruitSystem, vacancy, response);
            initLanguages(recruitSystem, vacancy, response);

            vacancies.add(vacancy);

            themeRepo.merge(theme);
        }
        vacancyRepo.persistAllAndFlush(vacancies);
        return vacancies;
    }

    private void initLanguages(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init languages", vacancy.getSystemId());
        Set<Language> languages = new HashSet<>();
        for (HHVacancyLanguage hhVacancyLanguage : response.getLanguages()) {
            LOGGER.debug("Checking language id - [{}]", hhVacancyLanguage.getId());
            Language language;
            if (languageRepo.existsByName(hhVacancyLanguage.getName().toLowerCase())) {
                LOGGER.debug("Language id - [{}] exists in database", hhVacancyLanguage.getId());
                LOGGER.debug("Language id - [{}] load from database", hhVacancyLanguage.getId());
                language = languageRepo.findFirstByName(hhVacancyLanguage.getName().toLowerCase());
            } else {
                LOGGER.debug("Language id - [{}] is new", hhVacancyLanguage.getId());
                language = new Language();
                language.setRecruitSystem(recruitSystem);
                language.setSystemId(hhVacancyLanguage.getId());
                language.setName(hhVacancyLanguage.getName().toLowerCase());
                LOGGER.debug("Language id - [{}] save to database", hhVacancyLanguage.getId());
                languageRepo.persist(language);
            }
            language.getVacancies().add(vacancy);
            languages.add(language);
        }
        vacancy.setLanguages(languages);
    }

    private void initSchedule(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init schedule", vacancy.getSystemId());
        if (Objects.nonNull(response.getLanguages())) {
            Schedule schedule;
            String scheduleName = response.getEmployment().getName();
            LOGGER.debug("Checking schedule name - [{}]", scheduleName);
            if (scheduleRepo.existsByName(scheduleName)) {
                LOGGER.debug("Schedule name - [{}] exists in database", scheduleName);
                LOGGER.debug("Schedule name - [{}] load from database", scheduleName);
                schedule = scheduleRepo.findFirstByName(scheduleName);
            } else {
                LOGGER.debug("Schedule name - [{}] is new", scheduleName);
                schedule = new Schedule();
                schedule.setName(scheduleName);
                schedule.setSystemId(response.getEmployment().getId());
                schedule.setRecruitSystem(recruitSystem);
                LOGGER.debug("Schedule name - [{}] save to database", scheduleName);
                scheduleRepo.persist(schedule);
            }
            vacancy.setSchedule(schedule);
        }
    }

    private void initProfessionalRole(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init professional roles", vacancy.getSystemId());
        Set<ProfessionalRole> professionalRoles = new HashSet<>();
        for (HHVacancyProfessionalRole role : response.getProfessionalRoles()) {
            ProfessionalRole professionalRole;
            if (professionalRoleRepo.existsByName(role.getName().toLowerCase())) {
                professionalRole = professionalRoleRepo.findFirstByName(role.getName().toLowerCase());
            } else {
                professionalRole = new ProfessionalRole();
                professionalRole.setRecruitSystem(recruitSystem);
                professionalRole.setName(role.getName().toLowerCase());
                professionalRoleRepo.persist(professionalRole);
            }
            professionalRole.getVacancies().add(vacancy);
            professionalRoles.add(professionalRole);
        }
        vacancy.setProfessionalRoles(professionalRoles);
    }

    private void initExperience(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init experience", vacancy.getSystemId());
        if (Objects.nonNull(response.getExperience())) {
            Experience experience;
            String experienceName = response.getExperience().getName();
            if (experienceRepo.existsByName(experienceName)) {
                experience = experienceRepo.findFirstByName(experienceName);
            } else {
                experience = new Experience();
                experience.setName(experienceName);
                experience.setSystemId(response.getExperience().getId());
                experience.setRecruitSystem(recruitSystem);
                experienceRepo.persist(experience);
            }
            vacancy.setExperience(experience);
        }
    }

    private void initEmployment(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init employment", vacancy.getSystemId());
        if (Objects.nonNull(response.getEmployment())) {
            Employment employment;
            String employmentName = response.getEmployment().getName();
            if (employmentRepo.existsByName(employmentName)) {
                employment = employmentRepo.findFirstByName(employmentName);
            } else {
                employment = new Employment();
                employment.setName(employmentName);
                employment.setSystemId(response.getEmployment().getId());
                employment.setRecruitSystem(recruitSystem);
                employmentRepo.persist(employment);
            }
            vacancy.setEmployment(employment);
        }
    }

    private void initEmployer(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init employer", vacancy.getSystemId());
        if (Objects.nonNull(response.getEmployer())) {
            Employer employer;
            String employerName = response.getEmployer().getName();
            if (employerRepo.existsByName(employerName)) {
                employer = employerRepo.findFirstByName(employerName);
            } else {
                employer = new Employer();
                employer.setName(employerName);
                employer.setSystemId(response.getEmployer().getId());
                employer.setRecruitSystem(recruitSystem);
                employerRepo.persist(employer);
            }
            vacancy.setEmployer(employer);
        }
    }

    private void initArea(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init area", vacancy.getSystemId());
        if (Objects.nonNull(response.getArea())) {
            Area area;
            String areaName = response.getArea().getName();
            if (areaRepo.existsByName(areaName)) {
                area = areaRepo.findFirstByName(areaName);
            } else {
                area = new Area();
                area.setName(areaName);
                area.setSystemId(response.getArea().getId());
                area.setRecruitSystem(recruitSystem);
                areaRepo.persist(area);
            }
            vacancy.setArea(area);
        }
    }

    private void initSalary(Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init salary", vacancy.getSystemId());
        if (Objects.nonNull(response.getSalary())) {
            Salary salary = new Salary();
            if (Objects.nonNull(response.getSalary())) {
                if (Objects.nonNull(response.getSalary().getFrom())){
                    salary.setMinVal(response.getSalary().getFrom().intValue());
                }
                if (Objects.nonNull(response.getSalary().getTo())){
                    salary.setMaxVal(response.getSalary().getTo().intValue());
                }
                salary.setCurrency(response.getSalary().getCurrency());
                salary.setGross(response.getSalary().isGross());
            }
            salary.setVacancy(vacancy);
            vacancy.setSalary(salary);
        }
    }

    private void initKeySkills(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        LOGGER.debug("Vacancy id - [{}]: init key skills", vacancy.getSystemId());
        for (HHVacancyKeySkill skill : response.getKeySkills()) {
            KeySkill keySkill;
            LOGGER.debug("Checking key skill name - [{}]", skill.getName());
            if (keySkillRepo.existsByName(skill.getName().toLowerCase())) {
                LOGGER.debug("Key skill name - [{}] exists and load from database", skill.getName());
                keySkill = keySkillRepo.findFirstByName(skill.getName().toLowerCase());
            } else {
                LOGGER.debug("Key skill name - [{}] is new", skill.getName());
                keySkill = new KeySkill();
                keySkill.setRecruitSystem(recruitSystem);
                keySkill.setName(skill.getName().toLowerCase());
                LOGGER.debug("Key skill name - [{}] save to database", skill.getName());
                keySkillRepo.persist(keySkill);
            }
            keySkill.getVacancies().add(vacancy);
            vacancy.getSkills().add(keySkill);
        }
    }
}
