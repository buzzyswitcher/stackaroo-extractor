package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.RecruitSystem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitSystemRepo extends HiberRepo<RecruitSystem>, CrudRepository<RecruitSystem, Integer> {

    RecruitSystem findFirstByName(String name);
}
