package com.example.bank.proj.commandfolder.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.commands.MoneyDepositCommand;
import com.example.bank.proj.commandfolder.commands.MoneyTransferCommand;
import com.example.bank.proj.commandfolder.commands.MoneyWithdrawCommand;
import com.example.bank.proj.commandfolder.dto.SuccessMessage;
import com.example.bank.proj.commandfolder.dto.TransactionIdGenerator;
import com.example.bank.proj.commandfolder.service.CommandService;


@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CommandService commandService; // interface

    public AccountController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/create")
    public SuccessMessage createAccount(@RequestBody CreateAccountCommand command) {
        try {
            return commandService.createAccount(command);
        } catch (Exception e) {
            return new SuccessMessage("Error creating account: " + e.getMessage(), null, false, 500);
        }
    }

    @PostMapping("/deposit")
    public SuccessMessage depositMoney(@RequestBody MoneyDepositCommand message) {
        try {
            BigDecimal amount = message.getAmount();
            String accountNumber = message.getAccountNumber();
            String id = message.getId();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new SuccessMessage("Error processing deposit: Amount must be greater than zero.", null, false, 400);
            }
            if (accountNumber == null || accountNumber.isEmpty()) {
                return new SuccessMessage("Error processing deposit: Amount must be greater than zero.", null, false, 400);
            }
            if (id == null || id.isEmpty()) {
                return new SuccessMessage("Error processing deposit: Transaction ID is required.", null, false, 400);
            }

            SuccessMessage s = commandService.fundDeposit(accountNumber, amount, id);
            return s;

        } catch (Exception e) {
            return new SuccessMessage("Internal Server Error", null, false, 500);
        }
    }

    @PostMapping("/withdraw")
    public SuccessMessage withdrawMoney(@RequestBody MoneyWithdrawCommand message) {
        try {
            BigDecimal amount = message.getAmount();
            String accountNumber = message.getAccountNumber();
            String id = message.getId();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new SuccessMessage("Error processing withdraw: Amount must be greater than zero.", null, false, 400);
            }
            if (accountNumber == null || accountNumber.isEmpty()) {
                return new SuccessMessage("Error processing withdraw: Amount must be greater than zero.", null, false, 400);
            }
            if (id == null || id.isEmpty()) {
                return new SuccessMessage("Error processing withdraw: Transaction ID is required.", null, false, 400);
            }

            SuccessMessage s = commandService.moneyWithdraw(accountNumber, amount, id);
            return s;

        } catch (Exception e) {
            return new SuccessMessage("Internal Server Error", null, false, 500);
        }
    }

    @PostMapping("/transfer")
    public SuccessMessage transferMoney(@RequestBody MoneyTransferCommand command) {
        
        try {
            BigDecimal amount = command.getAmount();
            String sourceAccountNumber = command.getSourceAccountNumber();
            String destinationAccountNumber = command.getDestinationAccountNumber();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new SuccessMessage("Error processing transfer: Amount must be greater than zero.", null, false, 400);
            }
            if (sourceAccountNumber == null || sourceAccountNumber.isEmpty()) {
                return new SuccessMessage("Error processing transfer: Source account number is required.", null, false, 400);
            }
            if (destinationAccountNumber == null || destinationAccountNumber.isEmpty()) {
                return new SuccessMessage("Error processing transfer: Destination account number is required.", null, false, 400);
            }
            if (sourceAccountNumber.equals(destinationAccountNumber)) {
                return new SuccessMessage("Error processing transfer: Source and destination account numbers must be different.", null, false, 400);
            }

            String transactionId = TransactionIdGenerator.generateTransactionId();
            SuccessMessage s = commandService.accountToAccountTransfer(sourceAccountNumber, destinationAccountNumber, amount, transactionId);
            return s;

        } catch (Exception e) {
            return new SuccessMessage("Internal Server Error", null, false, 500);
        }
    }
    
    @GetMapping("/accountHistory/{id}")
    public SuccessMessage getAccountHistory(@PathVariable String id) {
        return commandService.getAccountHistory(id);
    }

    @GetMapping("/accountReplay/{id}")
        public SuccessMessage getAccountReply(@PathVariable String id){
            return commandService.replayEvents(id);
        
    }
}
