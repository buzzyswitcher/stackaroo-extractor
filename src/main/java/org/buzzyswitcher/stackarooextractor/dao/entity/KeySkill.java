package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table
@EqualsAndHashCode(exclude = "vacancies")
public class KeySkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "recruit_system_id", nullable = false)
    private RecruitSystem recruitSystem;

    private String name;

    @ManyToMany(mappedBy = "skills")
    private Set<Vacancy> vacancies = new HashSet<>();
}
