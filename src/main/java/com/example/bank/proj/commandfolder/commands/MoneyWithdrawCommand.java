package com.example.bank.proj.commandfolder.commands;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyWithdrawCommand {
    private String accountNumber;
    private BigDecimal amount; // Using String to avoid floating-point issues
    private String id;
}
