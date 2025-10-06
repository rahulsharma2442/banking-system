package com.example.bank.proj.queryfolder.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bank.proj.commandfolder.events.AccountCreatedEvent;
import com.example.bank.proj.queryfolder.readmodel.AccountReadModel;
import com.example.bank.proj.queryfolder.repository.AccountReadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AccountReadServiceImp implements AccountReadService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountReadRepository accountReadRepository;

    public void processAccountCreatedEvent(String message) {
        try {
            System.out.println("📩 Received Redis message: " + message);

            AccountCreatedEvent event = objectMapper.readValue(message, AccountCreatedEvent.class);

            // Idempotency: skip if already processed
            if (accountReadRepository.existsById(event.getAccountId())) {
                System.out.println("⚠️ Account ID " + event.getAccountId() + " already exists. Skipping.");
                return;}

            AccountReadModel readModel = new AccountReadModel();
            readModel.setId(event.getAccountId());
            readModel.setUserId(event.getUserId());
            readModel.setAccountType(event.getAccountType());
            readModel.setBalance(event.getInitialDeposit());
            readModel.setCreatedAt(event.getTimestamp());

            accountReadRepository.save(readModel);
            System.out.println("✅ Read Model Updated for Account ID: " + event.getAccountId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<AccountReadModel> getAllAccounts() {
        return accountReadRepository.findAll(); 
    }
}
