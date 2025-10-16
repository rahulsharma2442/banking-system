package com.example.bank.proj.commandfolder.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
import com.example.bank.proj.commandfolder.dto.SuccessMessage;
import com.example.bank.proj.commandfolder.entites.Account;
import com.example.bank.proj.commandfolder.entites.EventStore;
import com.example.bank.proj.commandfolder.entites.User;
import com.example.bank.proj.commandfolder.events.AccountCreatedEvent;
import com.example.bank.proj.commandfolder.events.MoneyDepositEvent;
import com.example.bank.proj.commandfolder.events.MoneyTransferInEvent;
import com.example.bank.proj.commandfolder.events.MoneyTransferOutEvent;
import com.example.bank.proj.commandfolder.repositorioes.AccountRepository;
import com.example.bank.proj.commandfolder.repositorioes.EventStoreRepository;
import com.example.bank.proj.commandfolder.repositorioes.UserRepository;
import com.example.bank.proj.sharedfolder.publisher.AccountEventPublisher;

import jakarta.transaction.Transactional;

@Service
public class CommandServiceImp implements CommandService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EventStoreRepository eventStoreRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountEventPublisher eventPublisher;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public SuccessMessage createAccount(CreateAccountCommand command) {
        // ✅ Use Optional handling properly
        User user = userRepository.findById(command.getUserId())
                .orElse(null);
        System.out.println("User found: " + user);
        if (user == null) {
            System.out.println("User not found with ID: " + command.getUserId());
            return new SuccessMessage("User not found", null, false, 404);
        }

        // ✅ Avoid duplicate account creation
        List<Account> alreadyExists = accountRepository.findByUserId(command.getUserId());
        if (!alreadyExists.isEmpty()) {
            return new SuccessMessage("Account already exists", null, false, 409);
        }

        // ✅ Create new account
        Account account = new Account();
        account.setUser(user);
        account.setAccountType(command.getAccountType());
        account.setBalance(command.getInitialDeposit());
        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        // ✅ Use count() + 1 for account number generation (still fine for demo)
        String accountNumber = String.format("%08d", accountRepository.count() + 1);
        account.setAccountNumber(accountNumber);

        account = accountRepository.save(account);

        // ✅ Create and store event
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountNumber(),
                account.getUser().getId(),
                account.getAccountType(),
                account.getBalance(),
                account.getCreatedAt()
        );

        EventStore eventStore = new EventStore();
        eventStore.setEventType("AccountCreatedEvent");
        eventStore.setEventData(event.toString());
        eventStore.setCreatedAt(LocalDateTime.now());
        eventStore.setAggregateId(account.getAccountNumber());
        eventStore.setAggregateType("Account");
        eventStoreRepository.save(eventStore);

        // ✅ Publish to Redis
        eventPublisher.publishAccountEvent("accountEvents", event);

        return new SuccessMessage("Account created successfully", account, true, 201);
    }

    @Override
    public SuccessMessage userRegistration(UserRegisterCommand command) {
        User user = new User();
        user.setName(command.getUsername());
        user.setPassword(command.getPassword());
        user.setEmail(command.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("USER");
        user.setEnabled(true);

        userRepository.save(user);
        return new SuccessMessage("User registered successfully", user, true, 201);
    }

    @Override
    @Transactional
    public SuccessMessage fundDeposit(String accountNumber, BigDecimal amount, String transactionId) {
        try {
            String key = "txn:" + transactionId;

            // Atomic idempotency check
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(key, "Done", 10, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(isNew)) {
                return new SuccessMessage("Transaction already processed", null, false, 409);
            }

            // Pessimistic lock on account
            Account account = accountRepository.findAccountForUpdate(accountNumber);
            if (account == null) {
                return new SuccessMessage("Account not found", null, false, 404);
            }

            // Update balance
            account.setBalance(account.getBalance().add(amount));
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

            // Publish MoneyDepositEvent
            MoneyDepositEvent event = new MoneyDepositEvent();
            event.setAccountNumber(accountNumber);
            event.setAmount(amount);
            event.setTransactionId(transactionId);
            event.setStatus("SUCCESS");
            event.setTimeStamp(LocalDateTime.now());

            EventStore eventStore = new EventStore();
            eventStore.setEventType("MoneyDepositEvent");
            eventStore.setEventData(event.toString());
            eventStore.setCreatedAt(LocalDateTime.now());
            eventStore.setAggregateId(account.getAccountNumber());
            eventStore.setAggregateType("Account");
            eventStoreRepository.save(eventStore);
            eventPublisher.publishAccountEvent("depositEvents", event);

            return new SuccessMessage("Amount deposited successfully", account, true, 200);

        } catch (Exception e) {
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

    @Override
    @Transactional
    public SuccessMessage moneyWithdraw(String accountNumber, BigDecimal amount, String transactionId) {
        // Implementation for money withdrawal
        try {
            String key = "txn:" + transactionId;

            // Atomic idempotency check
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(key, "Done", 10, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(isNew)) {
                return new SuccessMessage("Transaction already processed", null, false, 409);
            }

            // Pessimistic lock on account
            Account account = accountRepository.findAccountForUpdate(accountNumber);
            if (account == null) {
                return new SuccessMessage("Account not found", null, false, 404);
            }

            // Check for sufficient balance
            if (account.getBalance().compareTo(amount) < 0) {
                return new SuccessMessage("Insufficient balance", null, false, 400);
            }

            // Update balance
            account.setBalance(account.getBalance().subtract(amount));
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

            // Publish MoneyWithdrawEvent
            MoneyDepositEvent event = new MoneyDepositEvent();
            event.setAccountNumber(accountNumber);
            event.setAmount(amount);
            event.setTransactionId(transactionId);
            event.setStatus("SUCCESS");
            event.setTimeStamp(LocalDateTime.now());

            EventStore eventStore = new EventStore();
            eventStore.setEventType("MoneyWithdrawEvent");
            eventStore.setEventData(event.toString());
            eventStore.setCreatedAt(LocalDateTime.now());
            eventStore.setAggregateId(account.getAccountNumber());
            eventStore.setAggregateType("Account");
            eventStoreRepository.save(eventStore);
            eventPublisher.publishAccountEvent("withdrawEvents", event);

            return new SuccessMessage("Amount withdrawn successfully", account, true, 200);
        } catch (Exception e) {
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

    @Override
    @Transactional
    public SuccessMessage accountToAccountTransfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String transactionId) {
        // Implementation for account-to-account transfer
        try {
            String key = "txn:" + transactionId;

            // Atomic idempotency check
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(key, "Done", 10, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(isNew)) {
                return new SuccessMessage("Transaction already processed", null, false, 409);
            }

            // Pessimistic lock on both accounts (order by account number to avoid deadlocks)
            Account sourceAccount;
            Account destinationAccount;

            if (sourceAccountNumber.compareTo(destinationAccountNumber) < 0) {
                sourceAccount = accountRepository.findAccountForUpdate(sourceAccountNumber);
                destinationAccount = accountRepository.findAccountForUpdate(destinationAccountNumber);
            } else {
                destinationAccount = accountRepository.findAccountForUpdate(destinationAccountNumber);
                sourceAccount = accountRepository.findAccountForUpdate(sourceAccountNumber);
            }
            if (sourceAccount == null || destinationAccount == null) {
                return new SuccessMessage("One or both accounts not found", null, false, 404);
            }
            // Check for sufficient balance
            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                return new SuccessMessage("Insufficient balance in source account", null, false, 400);
            }
            // Check for sufficient balance in destination account
            if (destinationAccount.getBalance().compareTo(amount) < 0) {
                return new SuccessMessage("Insufficient balance in destination account", null, false, 400);
            }
            // Perform the transfer
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
            destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            MoneyTransferOutEvent event = new MoneyTransferOutEvent();
            event.setSourceAccountNumber(sourceAccountNumber);
            event.setDestinationAccountNumber(destinationAccountNumber);
            event.setAmount(amount);
            event.setTransactionId(transactionId);
            event.setStatus("SUCCESS");
            event.setTimeStamp(LocalDateTime.now());

            EventStore eventStore = new EventStore();
            eventStore.setEventType("AccountToAccountTransferEvent");
            eventStore.setEventData(event.toString());
            eventStore.setCreatedAt(LocalDateTime.now());
            eventStore.setAggregateId(sourceAccount.getAccountNumber());
            eventStore.setAggregateType("Account");

            MoneyTransferInEvent inEvent = new MoneyTransferInEvent();
            inEvent.setSourceAccountNumber(sourceAccountNumber);
            inEvent.setDestinationAccountNumber(destinationAccountNumber);
            inEvent.setAmount(amount);
            inEvent.setTransactionId(transactionId);
            inEvent.setStatus("SUCCESS");
            inEvent.setTimeStamp(LocalDateTime.now());
            eventStoreRepository.save(eventStore);
            eventPublisher.publishAccountEvent("transferOutEvents", event);
            eventPublisher.publishAccountEvent("transferInEvents", inEvent);
            Map<String,Object> data = new HashMap<>();
            data.put("sourceAccount", sourceAccount);
            data.put("destinationAccount", destinationAccount);
            data.put("amountTransferred", amount);
            data.put("transactionId", transactionId);
            data.put("timeStamp", LocalDateTime.now().toString());
            return new SuccessMessage("Transfer successful", data, true, 200);
        } catch (Exception e) {
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }

}

}