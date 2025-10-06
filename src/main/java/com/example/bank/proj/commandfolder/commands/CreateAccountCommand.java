package com.example.bank.proj.commandfolder.commands;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountCommand {
    private Long userId;
    private String accountType;
    private BigDecimal initialDeposit; // Using BigDecimal to avoid floating-point issues

}

