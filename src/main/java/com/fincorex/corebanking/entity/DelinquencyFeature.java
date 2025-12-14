package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "DELINQUENCY_FEATURE", schema = "BANKFUSION")
public class DelinquencyFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long delinquencyProfileID;
    private String profileDescription;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "delinquencyFeature"
    )
    @Builder.Default
    private List<DelinquencyStage> delinquencyStages = new ArrayList<>();

    @Version
    private Long version;
}
