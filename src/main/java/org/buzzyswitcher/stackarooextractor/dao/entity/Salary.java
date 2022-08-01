package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Entity
@Table
@EqualsAndHashCode(exclude = "vacancy")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "salary_generator")
    @SequenceGenerator(name="salary_generator", sequenceName = "salary_seq", allocationSize=50)
    private Integer id;

    private Integer minVal;
    private Integer maxVal;
    private Boolean gross;
    private String currency;

    @OneToOne(mappedBy = "salary", fetch = FetchType.LAZY)
    private Vacancy vacancy;
}
