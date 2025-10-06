package com.example.bank.proj.commandfolder.entites;

public enum TransactionType {
    CREDIT,        // Money coming into account (e.g., deposit, salary)
    DEBIT,         // Money going out (e.g., withdrawal, bill payment)
    TRANSFER,      // Internal transfer A → B
    CHEQUE,        // Cheque-based transaction
    EXTERNAL       // External bank or party transaction
}
