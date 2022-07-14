package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Employer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployerRepo extends CrudRepository<Employer, Integer> {

    boolean existsByName(String name);
    Employer findFirstByName(String name);
}
