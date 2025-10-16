package com.example.bank.proj.commandfolder.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyTransferOutEvent {
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String transactionId;
    private String status; // e.g., "SUCCESS", "FAILED"
    private java.math.BigDecimal amount;
    private java.time.LocalDateTime timeStamp;
}