package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table
@EqualsAndHashCode(exclude = "vacancies")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class KeySkill implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "key_skill_generator")
    @SequenceGenerator(name="key_skill_generator", sequenceName = "key_skill_seq", allocationSize=50)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_system_id", nullable = false)
    private RecruitSystem recruitSystem;

    private String name;

    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    private Set<Vacancy> vacancies = new HashSet<>();
}
