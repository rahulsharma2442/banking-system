package com.example.bank.proj.commandfolder.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.service.CommandService;
import com.example.bank.proj.commandfolder.commands.MoneyDepositCommand;
import com.example.bank.proj.commandfolder.dto.SuccessMessage;    
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CommandService commandService; // interface

    public AccountController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/create")
    public String createAccount(@RequestBody CreateAccountCommand command) {
        try {
            commandService.createAccount(command);
            return "Account created successfully!";
        } catch (Exception e) {
            return "Error creating account: " + e.getMessage();
        }
    }

    @PostMapping("/deposit")
    public SuccessMessage depositMoney(@RequestBody MoneyDepositCommand message) {
        try {
            BigDecimal amount = message.getAmount();
            String accountNumber = message.getAccountNumber();
            String id = message.getId();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
               return new SuccessMessage("Error processing deposit: Amount must be greater than zero.",null, false, 400);
            }
            if(accountNumber == null || accountNumber.isEmpty()) {
                 return new SuccessMessage("Error processing deposit: Amount must be greater than zero.",null, false, 400);
            }
            if(id == null || id.isEmpty()) {
                return new SuccessMessage("Error processing deposit: Transaction ID is required.",null, false, 400);
            }

            SuccessMessage s = commandService.fundDeposit(accountNumber, amount, id);
            return s;

        } catch (Exception e) {
            return new SuccessMessage("Internal Server Error",null, false, 500);
        }
    }

}
