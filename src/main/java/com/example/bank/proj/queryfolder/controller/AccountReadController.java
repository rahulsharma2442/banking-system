package com.example.bank.proj.queryfolder.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.proj.queryfolder.service.AccountReadServiceImp;
@RestController
@RequestMapping("/read/accounts")
public class AccountReadController {
    
    @Autowired
    private AccountReadServiceImp accountReadServiceImp;

    @GetMapping
    public Object getAllAccounts() {
        return accountReadServiceImp.getAllAccounts();
    }
}
