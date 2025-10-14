package com.example.bank.proj.commandfolder.service;
import java.math.BigDecimal;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
import com.example.bank.proj.commandfolder.dto.SuccessMessage;
public interface CommandService {
    void createAccount(CreateAccountCommand command);
    void userRegistration(UserRegisterCommand command);
    SuccessMessage fundDeposit(String accountNumber, BigDecimal amount, String transactionId);
}
