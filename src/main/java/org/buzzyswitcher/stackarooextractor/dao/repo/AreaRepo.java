package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Area;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepo extends HiberRepo<Area>, CrudRepository<Area, Integer> {

    boolean existsByName(String name);
    Area findFirstByName(String name);
}
