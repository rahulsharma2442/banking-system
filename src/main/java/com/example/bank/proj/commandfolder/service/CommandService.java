package com.example.bank.proj.commandfolder.service;
import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
public interface CommandService {
    void createAccount(CreateAccountCommand command);
    void userRegistration(UserRegisterCommand command);
}
