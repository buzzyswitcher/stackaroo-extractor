package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table
@Entity
@Data
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "recruit_system_id", nullable = false)
    private RecruitSystem recruitSystem;

    private String systemId;

    @Column(length = 10000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne
    @JoinColumn(name = "employment_id")
    private Employment employment;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private Employer employer;

    @ManyToOne
    @JoinColumn(name = "experience_id")
    private Experience experience;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne
    @JoinColumn(name = "professional_role_id")
    private ProfessionalRole professionalRole;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "vacancy_skill",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<KeySkill> skills = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "salary_id", referencedColumnName = "id")
    private Salary salary;

    private LocalDateTime initialCreatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
