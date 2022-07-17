package org.buzzyswitcher.stackarooextractor.dao.entity.nsi;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.buzzyswitcher.stackarooextractor.dao.entity.Vacancy;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(schema = "nsi")
@Data
@EqualsAndHashCode(exclude = "vacancies")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(value = EnumType.STRING)
    private ThemeEnum theme;

    @ManyToMany(mappedBy = "themes")
    private Set<Vacancy> vacancies = new HashSet<>();
}
