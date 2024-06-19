package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.LanguageLevel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageLevelRepo extends HiberRepo<LanguageLevel>, CrudRepository<LanguageLevel, Integer> {

    boolean existsByName(String name);
    LanguageLevel findFirstByName(String name);
}
