package com.fincorex.corebanking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "INTERESTAPPLICATIONTAG", schema = "BANKFUSION")
public class InterestApplicationTag {
    @Id
    private String accountID;
    private Long rowSequence;
}
