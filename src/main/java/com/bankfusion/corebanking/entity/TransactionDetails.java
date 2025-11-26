package com.bankfusion.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TRANSACTION_DETAILS", schema = "BANKFUSION")
public class TransactionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountID;
    private BigDecimal amount;
    private String transactionID;
    private Character debitCreditFlag;
    private String transactionCode;
    private Date transactionDate;
    private String narration;

    @Version
    private Long version;
}
