package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    @Builder.Default
    private List<Account> accountList = new ArrayList<>();

    @Version
    private Long version;
}
