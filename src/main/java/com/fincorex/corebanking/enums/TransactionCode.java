
package com.fincorex.corebanking.enums;

public enum TransactionCode {
    A00("Account Transfer"),
    RPO("Loan Repayment"),
    LD0("Loan Disbursement"),
    I00("Interest Application"),
    IA0("Interest Accrual"),
    IA1("Interest Accrual Reversal"),
    FD0("Fixture Deposit"),
    FD1("Fixture Withdrawal");

    private final String narration;

    TransactionCode(String narration) {
        this.narration = narration;
    }

    public String getNarration() {
        return narration;
    }
}
