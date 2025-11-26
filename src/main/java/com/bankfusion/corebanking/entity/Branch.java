package com.bankfusion.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "BRANCH", schema = "BANKFUSION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Branch {
    @Id
    private String branchCode;
    private String branchName;
    private Boolean isHeadOffice;

    @OneToMany(
            mappedBy = "branch",
            cascade = CascadeType.ALL
    )
    private List<Account> accountList;

    @Version
    private Long version;
}
