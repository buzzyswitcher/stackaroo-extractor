package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Entity
@Table
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employment_generator")
    @SequenceGenerator(name="employment_generator", sequenceName = "employment_seq", allocationSize=50)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_system_id", nullable = false)
    private RecruitSystem recruitSystem;

    private String systemId;
    private String name;
}
