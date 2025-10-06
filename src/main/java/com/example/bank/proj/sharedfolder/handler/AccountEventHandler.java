package com.example.bank.proj.sharedfolder.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bank.proj.queryfolder.service.AccountReadService;

@Service
public class AccountEventHandler {
      @Autowired
    private AccountReadService accountReadService;

    public void handleMessage(String message) {
        // message is a JSON string; pass to service for processing
        accountReadService.processAccountCreatedEvent(message);
    }
}
