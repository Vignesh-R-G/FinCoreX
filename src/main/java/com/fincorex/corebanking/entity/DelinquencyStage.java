package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "DELINQUENCY_STAGE", schema = "BANKFUSION")
public class DelinquencyStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stageID;
    private String stageDescription;
    private Long overDueDays;

    @ManyToOne
    @JoinColumn(
            name = "delinquency_profile",
            referencedColumnName = "delinquencyProfileID"
    )
    private DelinquencyFeature delinquencyFeature;

    @Version
    private Long version;
}
