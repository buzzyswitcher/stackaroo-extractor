package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Language;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepo extends CrudRepository<Language, Integer> {

    boolean existsByName(String name);
    Language findFirstByName(String name);
}
