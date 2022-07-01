package org.buzzyswitcher.stackarooextractor.hh;

import lombok.Data;
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
import org.buzzyswitcher.stackarooextractor.model.HHVacancyKeySkill;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyResponse;
import org.buzzyswitcher.stackarooextractor.urlconstructor.AbstractUrlCreator;
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
import java.time.LocalDate;
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
    AbstractUrlCreator hhUrlCreator;
    VacancyRepo vacancyRepo;
    KeySkillRepo keySkillRepo;
    RecruitSystemRepo recruitSystemRepo;

    @PersistenceContext
    EntityManager em;

    public HHInteractor(RestTemplate restTemplate, AbstractUrlCreator hhUrlCreator, KeySkillRepo keySkillRepo,
                        RecruitSystemRepo recruitSystemRepo, VacancyRepo vacancyRepo) {
        this.restTemplate = restTemplate;
        this.hhUrlCreator = hhUrlCreator;
        this.keySkillRepo = keySkillRepo;
        this.recruitSystemRepo = recruitSystemRepo;
        this.vacancyRepo = vacancyRepo;
    }

    @Scheduled(initialDelay = 1000 * 10, fixedDelay=Long.MAX_VALUE)
    @Transactional
    public void startProcess() {
        LOGGER.info("START!!!!");
        Set<String> ids = getIds();
        interact(ids);
    }

    public void interact(Set<String> ids) {
        RecruitSystem recruitSystem = new RecruitSystem();
        recruitSystem.setName("HeadHunter");
        recruitSystemRepo.save(recruitSystem);
        em.flush();
        for (String id : ids) {
            Vacancy vacancy = null;
            Vacancy vacancyFromDB = vacancyRepo.findFirstBySystemId(id);
            if (vacancyRepo.existsBySystemId(id)) {
                continue;
            } else {
                vacancy = new Vacancy();
            }
            HHVacancyResponse response = restTemplate.getForEntity("https://api.hh.ru/vacancies/" + id, HHVacancyResponse.class).getBody();

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
        int maxPage = getMaxPagesForQuery();
        LOGGER.info("Max page: [{}}", maxPage);
        int page = 0;
        while (page <= maxPage) {
            QueryParams params = new QueryParams("Дизайнер интерьера", null, String.valueOf(page), "100", null, null);
            List<NameValuePair> queryParams = params.getParams();

            String url = hhUrlCreator.build(queryParams);
            try {
                url = URLDecoder.decode(url.toString(), "UTF-8"); // java.net class
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LOGGER.info("Current URL: [{}]", url);
            ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
            Set<String> idInPage = response.getBody().getItems().stream()
                    .map(item -> item.getId())
                    .collect(Collectors.toSet());
            ids.addAll(idInPage);
            page++;
        }
        LOGGER.info("ID's: [{}]", ids.toString());

        return ids;
    }

    private int getMaxPagesForQuery() {
        QueryParams params = new QueryParams("Дизайнер интерьера", null, "0", "100", null, null);
        List<NameValuePair> queryParams = params.getParams();

        String url = hhUrlCreator.build(queryParams);
        ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
        return response.getBody().getPages();
    }

    @Data
    public static class QueryParams {
        String text;
        String areaId;
        String currentPage;
        String itemsOnPage;
        LocalDate dateFrom;
        LocalDate dateTo;

        public QueryParams(String text, String areaId, String currentPage, String itemsOnPage, LocalDate dateFrom, LocalDate dateTo) {
            this.text = text;
            this.areaId = areaId;
            this.currentPage = currentPage;
            this.itemsOnPage = itemsOnPage;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }

        public List<NameValuePair> getParams() {
            List<NameValuePair> params = new ArrayList<>();
            if (text != null) params.add(new BasicNameValuePair(HHUrlCreator.TEXT, text));
            if (areaId != null) params.add(new BasicNameValuePair(HHUrlCreator.AREA_ID, areaId));
            if (currentPage != null) params.add(new BasicNameValuePair(HHUrlCreator.CURRENT_PAGE, currentPage));
            if (itemsOnPage != null) params.add(new BasicNameValuePair(HHUrlCreator.ITEMS_ON_PAGE, itemsOnPage));
            if (dateFrom != null) params.add(new BasicNameValuePair(HHUrlCreator.DATE_FROM, dateFrom.toString()));
            if (dateTo != null) params.add(new BasicNameValuePair(HHUrlCreator.DATE_TO, dateTo.toString()));
            return params;
        }
    }

}
