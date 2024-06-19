package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyRepo extends HiberRepo<Vacancy>, JpaRepository<Vacancy, Integer> {

    Vacancy findFirstBySystemId(String systemId);
    Boolean existsBySystemId(String systemId);

    @Query(value = "select exists (select t.id " +
            "from public.vacancy v " +
            "   inner join public.vacancy_theme vt on v.id = vt.vacancy_id  " +
            "   inner join nsi.theme t on vt.theme_id = t.id " +
            "where t.theme = ?1 and v.system_id = ?2) ", nativeQuery = true)
    boolean containingTheme(String stringTheme, String vacancySystemId);
}
