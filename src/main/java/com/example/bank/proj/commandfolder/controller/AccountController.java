package com.example.bank.proj.commandfolder.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.service.CommandService;

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

}
