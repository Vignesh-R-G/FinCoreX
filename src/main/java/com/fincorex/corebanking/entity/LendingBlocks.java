package com.fincorex.corebanking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "LENDING_BLOCKS")
public class LendingBlocks {
    @Id
    private String loanAccountID;
    private String settlementAccountID;
    private BigDecimal blockAmount;
}
