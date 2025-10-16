package com.example.bank.proj.queryfolder.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.bank.proj.commandfolder.events.AccountCreatedEvent;
import com.example.bank.proj.commandfolder.events.MoneyDepositEvent;
import com.example.bank.proj.commandfolder.events.MoneyWithdrawEvent;
import com.example.bank.proj.queryfolder.readmodel.AccountReadModel;
import com.example.bank.proj.queryfolder.repository.AccountReadRepository;
import com.example.bank.proj.commandfolder.events.MoneyTransferOutEvent;
import com.example.bank.proj.commandfolder.events.MoneyTransferInEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AccountReadServiceImp implements AccountReadService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountReadRepository accountReadRepository;

    @Async
    @Override
    public void processAccountCreatedEvent(String message) {
        try {
            System.out.println("📩 Received Redis message: " + message);

            AccountCreatedEvent event = objectMapper.readValue(message, AccountCreatedEvent.class);

            // Idempotency: skip if already processed
            AccountReadModel existingAccount = accountReadRepository.findByAccountNumber(event.getAccountNumber()).orElse(null);
            if (existingAccount != null) {
                System.out.println("⚠️ Account with Account Number " + event.getAccountNumber() + " already exists. Skipping.");
                return;
            }

            AccountReadModel readModel = new AccountReadModel();
            readModel.setAccountNumber(event.getAccountNumber());
            readModel.setUserId(event.getUserId());
            readModel.setAccountType(event.getAccountType());
            readModel.setBalance(event.getInitialDeposit());
            readModel.setCreatedAt(event.getTimestamp());

            accountReadRepository.save(readModel);
            System.out.println("✅ Read Model Updated for Account Number: " + event.getAccountNumber());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void moneyDepositEvent(String message) {
        try {
            System.out.println("📩 Received Redis message: " + message);

            MoneyDepositEvent event = objectMapper.readValue(message, MoneyDepositEvent.class);

            // Find the account by MySQL ID
            AccountReadModel readModel = accountReadRepository.findByAccountNumber(event.getAccountNumber()).orElse(null);

            if (readModel == null) {
                System.out.println("⚠️ No account found for ID " + event.getAccountNumber());
                return;
            }

            // Update balance
            BigDecimal newBalance = readModel.getBalance().add(event.getAmount());
            readModel.setBalance(newBalance);
            readModel.setUpdatedAt(event.getTimeStamp());

            // Save updated read model
            accountReadRepository.save(readModel);

            System.out.println("✅ Balance updated for Account ID: " + event.getAccountNumber()
                    + " | New Balance: " + newBalance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void moneyWithdrawEvent(String message) {
        try {
            System.out.println("📩 Received Redis message: " + message);

            // Deserialize the event from JSON
            MoneyWithdrawEvent event = objectMapper.readValue(message, MoneyWithdrawEvent.class);

            // Find the account by account number
            AccountReadModel readModel = accountReadRepository.findByAccountNumber(event.getAccountNumber()).orElse(null);

            if (readModel == null) {
                System.out.println("⚠️ No account found for Account Number: " + event.getAccountNumber());
                return;
            }

            // Check for sufficient balance
            if (readModel.getBalance().compareTo(event.getAmount()) < 0) {
                System.out.println("⚠️ Insufficient balance for Account Number: " + event.getAccountNumber());
                return;
            }

            // Deduct amount and update
            BigDecimal newBalance = readModel.getBalance().subtract(event.getAmount());
            readModel.setBalance(newBalance);
            readModel.setUpdatedAt(event.getTimeStamp());

            // Save updated read model
            accountReadRepository.save(readModel);

            System.out.println("✅ Withdrawal processed for Account: " + event.getAccountNumber()
                    + " | New Balance: " + newBalance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moneyTransferInEvent(String message) {
        System.out.println("Money Transfer In Event received: " + message);
        try {
            MoneyTransferInEvent event = objectMapper.readValue(message, MoneyTransferInEvent.class);
            AccountReadModel readModel = accountReadRepository.findByAccountNumber(event.getDestinationAccountNumber()).orElse(null);
            if (readModel == null) {
                System.out.println("⚠️ No account found for Destination Account Number: " + event.getDestinationAccountNumber());
                return;
            }
            BigDecimal newBalance = readModel.getBalance().add(event.getAmount());
            readModel.setBalance(newBalance);
            readModel.setUpdatedAt(event.getTimeStamp());
            // Save updated read model
            accountReadRepository.save(readModel);
            System.out.println("✅ Money Transfer In processed for Account: " + event.getDestinationAccountNumber()
                    + " | New Balance: " + newBalance);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void moneyTransferOutEvent(String message) {
        try {
            System.out.println("Money Transfer Out Event received: " + message);
            MoneyTransferOutEvent event = objectMapper.readValue(message, MoneyTransferOutEvent.class);
            AccountReadModel readModel = accountReadRepository.findByAccountNumber(event.getSourceAccountNumber()).orElse(null);
            if (readModel == null) {
                System.out.println("⚠️ No account found for Source Account Number: " + event.getSourceAccountNumber());
                return;
            }
            if (readModel.getBalance().compareTo(event.getAmount()) < 0) {
                System.out.println("⚠️ Insufficient balance for Account Number: " + event.getSourceAccountNumber());
                return;
            }
            BigDecimal newBalance = readModel.getBalance().subtract(event.getAmount());
            readModel.setBalance(newBalance);
            readModel.setUpdatedAt(event.getTimeStamp());

            // Save updated read model
            accountReadRepository.save(readModel);

            System.out.println("✅ Money Transfer Out processed for Account: " + event.getSourceAccountNumber()
                    + " | New Balance: " + newBalance);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<AccountReadModel> getAllAccounts() {
        return accountReadRepository.findAll();
    }
}
