package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.buzzyswitcher.stackarooextractor.dao.entity.KeySkill;
import org.buzzyswitcher.stackarooextractor.dao.entity.RecruitSystem;
import org.buzzyswitcher.stackarooextractor.dao.entity.Salary;
import org.buzzyswitcher.stackarooextractor.dao.entity.Vacancy;
import org.buzzyswitcher.stackarooextractor.dao.repo.KeySkillRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.RecruitSystemRepo;
import org.buzzyswitcher.stackarooextractor.dao.repo.VacancyRepo;
import org.buzzyswitcher.stackarooextractor.model.HHVacanciesIdResponse;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyId;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyKeySkill;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyResponse;
import org.buzzyswitcher.stackarooextractor.urlconstructor.UrlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @PersistenceContext
    EntityManager em;

    public HHInteractor(
            RestTemplate restTemplate,
            KeySkillRepo keySkillRepo,
            RecruitSystemRepo recruitSystemRepo,
            VacancyRepo vacancyRepo,
            UrlManager urlManager) {
        this.restTemplate = restTemplate;
        this.keySkillRepo = keySkillRepo;
        this.recruitSystemRepo = recruitSystemRepo;
        this.vacancyRepo = vacancyRepo;
        this.hhUrl = urlManager;
    }

    @Scheduled(initialDelay = 1000 * 10, fixedDelay=Long.MAX_VALUE)
    @Transactional
    public void startProcess() {
        LOGGER.info("START!!!!");
        Set<String> ids = getIds();
        interact(ids);
    }

    public void interact(Set<String> ids) {
        RecruitSystem recruitSystem = recruitSystemRepo.findFirstByName("HEAD_HUNTER");
        for (String id : ids) {
            Vacancy vacancy = null;

            if (vacancyRepo.existsBySystemId(id)) {
                continue;
            } else {
                vacancy = new Vacancy();
            }

            String url = hhUrl.getHHVacancy(id);
            HHVacancyResponse response = restTemplate.getForEntity(url, HHVacancyResponse.class).getBody();

            //Key-skills
            Set<KeySkill> skills = new HashSet<>();
            for (HHVacancyKeySkill skill : response.getKeySkills()) {
                KeySkill keySkill;
                if (keySkillRepo.existsByName(skill.getName().toLowerCase())) {
                    keySkill = keySkillRepo.findFirstByName(skill.getName().toLowerCase());
                } else {
                    keySkill = new KeySkill();
                    keySkill.setRecruitSystem(recruitSystem);
                    keySkill.setName(skill.getName().toLowerCase());
                }
                keySkill.getVacancies().add(vacancy);
                skills.add(keySkill);
            }

            //Salary
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

            vacancy.setSkills(skills);
            vacancy.setRecruitSystem(recruitSystem);
            vacancy.setSystemId(response.getId());
            vacancy.setDescription(response.getDescription());
            vacancy.setCreatedAt(LocalDateTime.parse(response.getCreatedAt(), formatter));
            vacancy.setInitialCreatedAt(LocalDateTime.parse(response.getInitialCreatedAt(), formatter));
            vacancy.setPublishedAt(LocalDateTime.parse(response.getPublishedAt(), formatter));
            vacancyRepo.save(vacancy);
            em.flush();
        }
    }

    public Set<String> getIds() {
        Set<String> ids = new HashSet<>();
        int page = 0;
        int pages = 19;
        while (page <= pages - 1) {
            List<NameValuePair> param = buildParamsForIdQuery("java", page, "100", "96");

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
            pages = response.getBody().getPages();
        }
        LOGGER.info("ID's: [{}]", ids.toString());

        return ids;
    }

    private static List<NameValuePair> buildParamsForIdQuery(String text, int currentPage, String itemsOnPage, String professionalRole) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(HHConfig.TEXT, text));
        params.add(new BasicNameValuePair(HHConfig.CURRENT_PAGE, String.valueOf(currentPage)));
        params.add(new BasicNameValuePair(HHConfig.ITEMS_ON_PAGE, itemsOnPage));
        params.add(new BasicNameValuePair(HHConfig.PROFESSIONAL_ROLE_ID, professionalRole));
        return params;
    }

    private int getMaxPages() {
        List<NameValuePair> params = buildParamsForIdQuery("java", 0, "100", "96");
        String url = hhUrl.getHHVacanciesId(params);
        ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
        return response.getBody().getPages();
    }
}
