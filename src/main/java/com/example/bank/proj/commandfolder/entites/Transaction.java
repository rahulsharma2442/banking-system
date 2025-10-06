package com.example.bank.proj.commandfolder.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // For account-to-account transfers: source → destination
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = true)
    private Account sourceAccount;  // null for deposits into the bank

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = true)
    private Account destinationAccount;  // null for withdrawals

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;  // CREDIT, DEBIT, TRANSFER, CHEQUE, EXTERNAL

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String reference;  // optional: cheque number, UPI ref, etc.

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
