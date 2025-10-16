package com.example.bank.proj.commandfolder.commands;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyTransferCommand {
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
}
