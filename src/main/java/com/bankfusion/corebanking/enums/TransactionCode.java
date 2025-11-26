
package com.bankfusion.corebanking.enums;

public enum TransactionCode {
    A00("Account Transfer"),
    RPO("Loan Repayment"),
    I00("Interest Application");

    private final String narration;

    TransactionCode(String narration) {
        this.narration = narration;
    }

    public String getNarration() {
        return narration;
    }
}
