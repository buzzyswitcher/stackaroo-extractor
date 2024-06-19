package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Employment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmploymentRepo extends HiberRepo<Employment>, CrudRepository<Employment, Integer> {

    boolean existsByName(String name);
    Employment findFirstByName(String name);
}
