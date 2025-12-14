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

@Table(name = "REPAYMENTCOLLECTIONTAG", schema = "BANKFUSION")
public class RepaymentCollectionTag {
    @Id
    private String loanAccountID;
    private Long rowSequence;
    private String settlementAccountID;
}
