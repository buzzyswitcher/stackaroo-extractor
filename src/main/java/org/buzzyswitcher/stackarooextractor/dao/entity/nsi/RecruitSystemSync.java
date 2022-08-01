package org.buzzyswitcher.stackarooextractor.dao.entity.nsi;

import lombok.Data;
import org.buzzyswitcher.stackarooextractor.dao.entity.RecruitSystem;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(schema = "nsi")
@Data
public class RecruitSystemSync {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name="recruit_system_sync_generator", sequenceName = "recruit_system_sync_seq", allocationSize=50)
    private Integer id;

    private LocalDateTime lastUpdateDateTime;

    @ManyToOne
    @JoinColumn(name = "recruit_system_id")
    private RecruitSystem recruitSystem;
}
