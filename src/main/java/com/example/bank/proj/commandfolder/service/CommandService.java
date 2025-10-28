package com.example.bank.proj.commandfolder.service;
import java.math.BigDecimal;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
import com.example.bank.proj.commandfolder.dto.SuccessMessage;
public interface CommandService {
    SuccessMessage createAccount(CreateAccountCommand command);
    SuccessMessage userRegistration(UserRegisterCommand command);
    SuccessMessage fundDeposit(String accountNumber, BigDecimal amount, String transactionId);
    SuccessMessage moneyWithdraw(String accountNumber, BigDecimal amount, String transactionId);
    SuccessMessage accountToAccountTransfer(String fromAccount, String toAccount, BigDecimal amount, String transactionId);
    SuccessMessage getAccountHistory(String AccountNumber);
    SuccessMessage replayEvents(String accountNumber);
    SuccessMessage getAccountInfoForUser(Long userId);
}