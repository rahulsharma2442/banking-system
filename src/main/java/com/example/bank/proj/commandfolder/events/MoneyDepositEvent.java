package com.example.bank.proj.commandfolder.events;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyDepositEvent {
    private String accountNumber;
    private BigDecimal amount;
    private String transactionId;
    private String status; // e.g., "SUCCESS", "FAILED"
    private String timeStamp;
}
