package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Experience;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienceRepo extends HiberRepo<Experience>, CrudRepository<Experience, Integer> {

    boolean existsByName(String name);
    Experience findFirstByName(String name);
}
