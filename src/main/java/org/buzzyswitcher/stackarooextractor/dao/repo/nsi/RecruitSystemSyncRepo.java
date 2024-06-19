package org.buzzyswitcher.stackarooextractor.dao.repo.nsi;

import org.buzzyswitcher.stackarooextractor.dao.entity.nsi.RecruitSystemSync;
import org.buzzyswitcher.stackarooextractor.dao.repo.HiberRepo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitSystemSyncRepo extends HiberRepo<RecruitSystemSync>, CrudRepository<RecruitSystemSync, Integer> {

    RecruitSystemSync findFirstByRecruitSystemNameOrderByLastUpdateDateTimeDesc(String recruitSystemName);
}
