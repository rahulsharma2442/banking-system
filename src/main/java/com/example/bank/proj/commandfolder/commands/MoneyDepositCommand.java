package com.example.bank.proj.commandfolder.commands;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyDepositCommand {
    private String accountNumber;
    private BigDecimal amount; // Using String to avoid floating-point issues
    private String id;
}
