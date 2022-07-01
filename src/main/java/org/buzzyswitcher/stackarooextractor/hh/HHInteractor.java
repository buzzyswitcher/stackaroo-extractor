package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.buzzyswitcher.stackarooextractor.dao.entity.KeySkill;
import org.buzzyswitcher.stackarooextractor.dao.entity.RecruitSystem;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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

    @Transactional
    public void interact(Set<String> ids) {
        RecruitSystem recruitSystem = new RecruitSystem();
        recruitSystem.setName("HeadHunter");
        recruitSystemRepo.save(recruitSystem);
        em.flush();
        for (String id : ids) {
            Vacancy vacancy = null;
            Vacancy vacancyFromDB = vacancyRepo.findFirstBySystemId(id);
            if (vacancyFromDB == null) {
                vacancy = new Vacancy();
            } else {
                vacancy = vacancyFromDB;
            }
            HHVacancyResponse response = restTemplate.getForEntity("https://api.hh.ru/vacancies/" + id, HHVacancyResponse.class).getBody();
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
            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair(HHUrlCreator.TEXT, "java"));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.AREA_ID, "4"));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.ITEMS_ON_PAGE, "100"));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_FROM, LocalDate.now().minusDays(1).toString()));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_TO, LocalDate.now().toString()));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.CURRENT_PAGE, String.valueOf(page)));
            String url = hhUrlCreator.build(queryParams);
            LOGGER.info("Current URL: [{}]", url);
            ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
            Set<String> idInPage = response.getBody().getItems().stream()
                    .map(item -> item.getId())
                    .collect(Collectors.toSet());
            ids.addAll(idInPage);
            page++;
        }

        return ids;
    }

    private int getMaxPagesForQuery() {
        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair(HHUrlCreator.TEXT, "java"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.AREA_ID, "4"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.CURRENT_PAGE, "0"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.ITEMS_ON_PAGE, "100"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_FROM, LocalDate.now().minusDays(1).toString()));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_TO, LocalDate.now().toString()));

        String url = hhUrlCreator.build(queryParams);
        ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
        return response.getBody().getPages();
    }

}
