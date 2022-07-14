package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.ProfessionalRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionalRoleRepo extends CrudRepository<ProfessionalRole, Integer> {

    boolean existsByName(String name);
    ProfessionalRole findFirstByName(String name);
}
