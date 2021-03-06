package org.buzzyswitcher.stackarooextractor.dao.repo;

import org.buzzyswitcher.stackarooextractor.dao.entity.Schedule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepo extends CrudRepository<Schedule, Integer> {

    boolean existsByName(String name);
    Schedule findFirstByName(String name);
}
