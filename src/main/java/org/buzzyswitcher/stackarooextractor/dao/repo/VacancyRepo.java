package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Vacancy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyRepo extends CrudRepository<Vacancy, Integer> {
}
