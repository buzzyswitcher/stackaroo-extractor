package org.buzzyswitcher.stackarooextractor.dao.repo.nsi;

import org.buzzyswitcher.stackarooextractor.dao.entity.nsi.Theme;
import org.buzzyswitcher.stackarooextractor.dao.entity.nsi.ThemeEnum;
import org.buzzyswitcher.stackarooextractor.dao.repo.HiberRepo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeRepo extends HiberRepo<Theme>, CrudRepository<Theme, Integer> {

    Theme findFirstByTheme(ThemeEnum theme);
}
