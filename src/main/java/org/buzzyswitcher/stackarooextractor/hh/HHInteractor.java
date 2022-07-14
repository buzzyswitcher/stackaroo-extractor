package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import org.buzzyswitcher.stackarooextractor.model.HHVacanciesIdResponse;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyId;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyKeySkill;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyLanguage;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyProfessionalRole;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyResponse;
import org.buzzyswitcher.stackarooextractor.urlconstructor.UrlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class HHInteractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HHInteractor.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    RestTemplate restTemplate;
    VacancyRepo vacancyRepo;
    KeySkillRepo keySkillRepo;
    RecruitSystemRepo recruitSystemRepo;
    HHUrl hhUrl;
    EmployerRepo employerRepo;
    EmploymentRepo employmentRepo;
    ExperienceRepo experienceRepo;
    ProfessionalRoleRepo professionalRoleRepo;
    ScheduleRepo scheduleRepo;
    LanguageRepo languageRepo;
    AreaRepo areaRepo;

    @PersistenceContext
    EntityManager em;

    public HHInteractor(
            RestTemplate restTemplate,
            KeySkillRepo keySkillRepo,
            RecruitSystemRepo recruitSystemRepo,
            VacancyRepo vacancyRepo,
            UrlManager urlManager,
            EmployerRepo employerRepo,
            EmploymentRepo employmentRepo,
            ExperienceRepo experienceRepo,
            ProfessionalRoleRepo professionalRoleRepo,
            ScheduleRepo scheduleRepo,
            LanguageRepo languageRepo,
            AreaRepo areaRepo) {
        this.restTemplate = restTemplate;
        this.keySkillRepo = keySkillRepo;
        this.recruitSystemRepo = recruitSystemRepo;
        this.vacancyRepo = vacancyRepo;
        this.hhUrl = urlManager;
        this.employerRepo = employerRepo;
        this.employmentRepo = employmentRepo;
        this.experienceRepo = experienceRepo;
        this.professionalRoleRepo = professionalRoleRepo;
        this.scheduleRepo = scheduleRepo;
        this.languageRepo = languageRepo;
        this.areaRepo = areaRepo;
    }

    @Scheduled(initialDelay = 1000 * 10, fixedDelay=Long.MAX_VALUE)
    @Transactional
    public void startProcess() {
        LOGGER.info("START!!!!");
        Set<String> ids = getIds();
        Set<Vacancy> vacancies = downloadVacancies(ids);
        vacancyRepo.saveAll(vacancies);
    }

    public Set<Vacancy> downloadVacancies(Set<String> ids) {
        RecruitSystem recruitSystem = recruitSystemRepo.findFirstByName("HEAD_HUNTER");
        Set<Vacancy> vacancies = new HashSet<>();
        for (String id : ids) {
            Vacancy vacancy = null;

            if (vacancyRepo.existsBySystemId(id)) {
                continue;
            } else {
                vacancy = new Vacancy();
            }

            String url = hhUrl.getHHVacancy(id);
            HHVacancyResponse response = restTemplate.getForEntity(url, HHVacancyResponse.class).getBody();

            vacancy.setRecruitSystem(recruitSystem);
            vacancy.setSystemId(response.getId());
            vacancy.setDescription(response.getDescription());
            vacancy.setCreatedAt(LocalDateTime.parse(response.getCreatedAt(), formatter));
            vacancy.setInitialCreatedAt(LocalDateTime.parse(response.getInitialCreatedAt(), formatter));
            vacancy.setPublishedAt(LocalDateTime.parse(response.getPublishedAt(), formatter));
            vacancy.setSyncAt(LocalDateTime.now());

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
        }
        return vacancies;
    }

    private void initLanguages(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        Set<Language> languages = new HashSet<>();
        for (HHVacancyLanguage hhVacancyLanguage : response.getLanguages()) {
            Language language;
            if (languageRepo.existsByName(hhVacancyLanguage.getName().toLowerCase())) {
                language = languageRepo.findFirstByName(hhVacancyLanguage.getName().toLowerCase());
            } else {
                language = new Language();
                language.setRecruitSystem(recruitSystem);
                language.setSystemId(hhVacancyLanguage.getId());
                language.setName(hhVacancyLanguage.getName().toLowerCase());
                languageRepo.save(language);
            }
            language.getVacancies().add(vacancy);
            languages.add(language);
        }
        vacancy.setLanguages(languages);
    }

    private void initSchedule(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        if (Objects.nonNull(response.getLanguages())) {
            Schedule schedule;
            String scheduleName = response.getEmployment().getName();
            if (scheduleRepo.existsByName(scheduleName)) {
                schedule = scheduleRepo.findFirstByName(scheduleName);
            } else {
                schedule = new Schedule();
                schedule.setName(scheduleName);
                schedule.setSystemId(response.getEmployment().getId());
                schedule.setRecruitSystem(recruitSystem);
                scheduleRepo.save(schedule);
            }
            vacancy.setSchedule(schedule);
        }
    }

    private void initProfessionalRole(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
        Set<ProfessionalRole> professionalRoles = new HashSet<>();
        for (HHVacancyProfessionalRole role : response.getProfessionalRoles()) {
            ProfessionalRole professionalRole;
            if (professionalRoleRepo.existsByName(role.getName().toLowerCase())) {
                professionalRole = professionalRoleRepo.findFirstByName(role.getName().toLowerCase());
            } else {
                professionalRole = new ProfessionalRole();
                professionalRole.setRecruitSystem(recruitSystem);
                professionalRole.setName(role.getName().toLowerCase());
                professionalRoleRepo.save(professionalRole);
            }
            professionalRole.getVacancies().add(vacancy);
            professionalRoles.add(professionalRole);
        }
        vacancy.setProfessionalRoles(professionalRoles);
    }

    private void initExperience(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
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
                experienceRepo.save(experience);
            }
            vacancy.setExperience(experience);
        }
    }

    private void initEmployment(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
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
                employmentRepo.save(employment);
            }
            vacancy.setEmployment(employment);
        }
    }

    private void initEmployer(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
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
                employerRepo.save(employer);
            }
            vacancy.setEmployer(employer);
        }
    }

    private void initArea(RecruitSystem recruitSystem, Vacancy vacancy, HHVacancyResponse response) {
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
                areaRepo.save(area);
            }
            vacancy.setArea(area);
        }
    }

    private void initSalary(Vacancy vacancy, HHVacancyResponse response) {
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
        Set<KeySkill> skills = new HashSet<>();
        for (HHVacancyKeySkill skill : response.getKeySkills()) {
            KeySkill keySkill;
            if (keySkillRepo.existsByName(skill.getName().toLowerCase())) {
                keySkill = keySkillRepo.findFirstByName(skill.getName().toLowerCase());
            } else {
                keySkill = new KeySkill();
                keySkill.setRecruitSystem(recruitSystem);
                keySkill.setName(skill.getName().toLowerCase());
                keySkillRepo.save(keySkill);
            }
            keySkill.getVacancies().add(vacancy);
            skills.add(keySkill);
        }
        vacancy.setSkills(skills);
    }

    public Set<String> getIds() {
        Set<String> ids = new HashSet<>();
        int page = 0;
        int pages = 19;
        while (page <= pages - 1) {
            List<NameValuePair> param = buildParamsForIdQuery("java", page, "100", "96", "4");

            String url = hhUrl.getHHVacanciesId(param);
            try {
                url = URLDecoder.decode(url.toString(), "UTF-8"); // java.net class
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LOGGER.info("Current URL: [{}], page: [{}], pages: [{}]", url, page, pages);
            ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
            Set<String> idInPage = response.getBody().getItems().stream()
                    .map(HHVacancyId::getId)
                    .collect(Collectors.toSet());
            ids.addAll(idInPage);
            page++;

            if (pages > response.getBody().getPages()) {
                pages = response.getBody().getPages();
            }
        }
        LOGGER.info("ID's: [{}]", ids.toString());

        return ids;
    }

    private static List<NameValuePair> buildParamsForIdQuery(String text, int currentPage, String itemsOnPage, String professionalRole, String area) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(HHConfig.TEXT, text));
        params.add(new BasicNameValuePair(HHConfig.AREA_ID, area));
        params.add(new BasicNameValuePair(HHConfig.CURRENT_PAGE, String.valueOf(currentPage)));
        params.add(new BasicNameValuePair(HHConfig.ITEMS_ON_PAGE, itemsOnPage));
        params.add(new BasicNameValuePair(HHConfig.PROFESSIONAL_ROLE_ID, professionalRole));
        return params;
    }

    private int getMaxPages() {
        List<NameValuePair> params = buildParamsForIdQuery("java", 0, "100", "96", "4");
        String url = hhUrl.getHHVacanciesId(params);
        ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
        return response.getBody().getPages();
    }
}
