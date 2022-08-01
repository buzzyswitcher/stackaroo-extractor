package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table
@EqualsAndHashCode(exclude = "vacancies")
public class ProfessionalRole {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "professional_role_generator")
    @SequenceGenerator(name="professional_role_generator", sequenceName = "professional_role_seq", allocationSize=50)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_system_id", nullable = false)
    private RecruitSystem recruitSystem;

    private String systemId;
    private String name;

    @ManyToMany(mappedBy = "professionalRoles", fetch = FetchType.LAZY)
    private Set<Vacancy> vacancies = new HashSet<>();
}
