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
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_generator")
    @SequenceGenerator(name="schedule_generator", sequenceName = "schedule_seq", allocationSize=50)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_system_id", nullable = false)
    private RecruitSystem recruitSystem;

    private String systemId;
    private String name;
}
