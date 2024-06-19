package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.KeySkill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeySkillRepo extends HiberRepo<KeySkill>, CrudRepository<KeySkill, Integer> {

    Boolean existsByName(String name);
    KeySkill findFirstByName(String name);
}
