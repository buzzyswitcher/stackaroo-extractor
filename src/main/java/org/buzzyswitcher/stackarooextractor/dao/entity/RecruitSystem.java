package org.buzzyswitcher.stackarooextractor.dao.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(schema = "nsi")
@Data
public class RecruitSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recruit_system_generator")
    @SequenceGenerator(name="recruit_system_generator", sequenceName = "recruit_system_seq", allocationSize=50)
    private Integer id;

    private String name;
}
