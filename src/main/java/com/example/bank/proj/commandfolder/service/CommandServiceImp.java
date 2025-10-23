package com.example.bank.proj.commandfolder.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.example.bank.proj.commandfolder.events.MoneyWithdrawEvent;
import com.example.bank.proj.commandfolder.repositorioes.AccountRepository;
import com.example.bank.proj.commandfolder.repositorioes.EventStoreRepository;
import com.example.bank.proj.commandfolder.repositorioes.UserRepository;
import com.example.bank.proj.sharedfolder.publisher.AccountEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SuccessMessage createAccount(CreateAccountCommand command) {
        try{
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
        account.setCreatedAt(LocalDateTime.now().toString());
        account.setUpdatedAt(LocalDateTime.now().toString());
       

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
        ObjectMapper mapper = new ObjectMapper();
        String jsonEvent = mapper.writeValueAsString(event);
        EventStore eventStore = new EventStore();
        eventStore.setEventType("AccountCreatedEvent");
        eventStore.setEventData(jsonEvent);
        eventStore.setCreatedAt(LocalDateTime.now().toString());
        eventStore.setAggregateId(account.getAccountNumber());
        eventStore.setAggregateType("Account");
        eventStoreRepository.save(eventStore);

        // ✅ Publish to Redis
        eventPublisher.publishAccountEvent("accountEvents", event);

        return new SuccessMessage("Account created successfully", account, true, 201);
        }
        catch(Exception e){
            e.printStackTrace();
            return new SuccessMessage("Error creating account: " + e.getMessage(), null, false, 500);
        }
        // ✅ Use Optional handling properly
      
    }

    @Override
    public SuccessMessage userRegistration(UserRegisterCommand command) {
        String token = UUID.randomUUID().toString()+"-"+LocalDateTime.now().toString();
        command.setPassword(passwordEncoder.encode(command.getPassword()));
        User user = new User();
        String uname = command.getEmail().split("@")[0]+UUID.randomUUID().toString().substring(0,5);
        user.setUsername(uname);
        user.setName(command.getName());
        user.setPassword(command.getPassword());
        user.setEmail(command.getEmail());
        user.setCreatedAt(LocalDateTime.now().toString());
        user.setUpdatedAt(LocalDateTime.now().toString());
        user.setRole("USER");
        user.setEnabled(true);
        user.setVerified(false);
        user.setToken(token);
         
        userRepository.save(user);
        // emailService.sendRegistrationEmail(user.getEmail(), "http://localhost:8080/api/v1/command/verify-email?token="+token);
        return new SuccessMessage("User registered successfully please verify your email", user, true, 201);
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
            account.setUpdatedAt(LocalDateTime.now().toString());
            accountRepository.save(account);

            // Create MoneyDepositEvent
            MoneyDepositEvent event = new MoneyDepositEvent();
            event.setAccountNumber(accountNumber);
            event.setAmount(amount);
            event.setTransactionId(transactionId);
            event.setStatus("SUCCESS");
            event.setTimeStamp(LocalDateTime.now().toString());

            // Serialize event to JSON before storing
            ObjectMapper mapper = new ObjectMapper();
            String jsonEvent = mapper.writeValueAsString(event);

            // Save to EventStore
            EventStore eventStore = new EventStore();
            eventStore.setEventType("MoneyDepositEvent");
            eventStore.setEventData(jsonEvent);   // <-- store JSON
            eventStore.setCreatedAt(LocalDateTime.now().toString());
            eventStore.setAggregateId(account.getAccountNumber());
            eventStore.setAggregateType("Account");
            eventStoreRepository.save(eventStore);

            // Publish event
            eventPublisher.publishAccountEvent("depositEvents", event);

            return new SuccessMessage("Amount deposited successfully", account, true, 200);

        } catch (Exception e) {
            e.printStackTrace();
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

    @Override
    @Transactional
    public SuccessMessage moneyWithdraw(String accountNumber, BigDecimal amount, String transactionId) {
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
            account.setUpdatedAt(LocalDateTime.now().toString());
            accountRepository.save(account);

            // Create MoneyWithdrawEvent
            MoneyWithdrawEvent event = new MoneyWithdrawEvent();
            event.setAccountNumber(accountNumber);
            event.setAmount(amount);
            event.setTransactionId(transactionId);
            event.setStatus("SUCCESS");
            event.setTimeStamp(LocalDateTime.now().toString());

            // Serialize to JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonEvent = mapper.writeValueAsString(event);

            // Save to EventStore
            EventStore eventStore = new EventStore();
            eventStore.setEventType("MoneyWithdrawEvent");
            eventStore.setEventData(jsonEvent); // <-- store JSON
            eventStore.setCreatedAt(LocalDateTime.now().toString());
            eventStore.setAggregateId(account.getAccountNumber());
            eventStore.setAggregateType("Account");
            eventStoreRepository.save(eventStore);

            // Publish event
            eventPublisher.publishAccountEvent("withdrawEvents", event);

            return new SuccessMessage("Amount withdrawn successfully", account, true, 200);
        } catch (Exception e) {
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

    @Override
    @Transactional
    public SuccessMessage accountToAccountTransfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String transactionId) {
        try {
            String key = "txn:" + transactionId;

            // Idempotency check
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "Done", 10, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(isNew)) {
                return new SuccessMessage("Transaction already processed", null, false, 409);
            }

            // Pessimistic lock on accounts to avoid deadlock
            Account sourceAccount, destinationAccount;
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

            // Perform transfer
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
            destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            ObjectMapper mapper = new ObjectMapper();

            // Out event for source account
            MoneyTransferOutEvent outEvent = new MoneyTransferOutEvent();
            outEvent.setSourceAccountNumber(sourceAccountNumber);
            outEvent.setDestinationAccountNumber(destinationAccountNumber);
            outEvent.setAmount(amount);
            outEvent.setTransactionId(transactionId);
            outEvent.setStatus("SUCCESS");
            outEvent.setTimeStamp(LocalDateTime.now().toString());

            EventStore outEventStore = new EventStore();
            outEventStore.setEventType("MoneyTransferOutEvent");
            outEventStore.setEventData(mapper.writeValueAsString(outEvent)); // serialize
            outEventStore.setCreatedAt(LocalDateTime.now().toString());
            outEventStore.setAggregateId(sourceAccountNumber);
            outEventStore.setAggregateType("Account");
            eventStoreRepository.save(outEventStore);
            eventPublisher.publishAccountEvent("transferOutEvents", outEvent);

            // In event for destination account
            MoneyTransferInEvent inEvent = new MoneyTransferInEvent();
            inEvent.setSourceAccountNumber(sourceAccountNumber);
            inEvent.setDestinationAccountNumber(destinationAccountNumber);
            inEvent.setAmount(amount);
            inEvent.setTransactionId(transactionId);
            inEvent.setStatus("SUCCESS");
            inEvent.setTimeStamp(LocalDateTime.now().toString());

            EventStore inEventStore = new EventStore();
            inEventStore.setEventType("MoneyTransferInEvent");
            inEventStore.setEventData(mapper.writeValueAsString(inEvent)); // serialize
            inEventStore.setCreatedAt(LocalDateTime.now().toString());
            inEventStore.setAggregateId(destinationAccountNumber);
            inEventStore.setAggregateType("Account");
            eventStoreRepository.save(inEventStore);
            eventPublisher.publishAccountEvent("transferInEvents", inEvent);

            // Prepare response
            Map<String, Object> data = new HashMap<>();
            data.put("sourceAccount", sourceAccount.getAccountNumber());
            data.put("destinationAccount", destinationAccount.getAccountNumber());
            data.put("amountTransferred", amount);
            data.put("transactionId", transactionId);
            data.put("timeStamp", LocalDateTime.now().toString());

            return new SuccessMessage("Transfer successful", data, true, 200);

        } catch (Exception e) {
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

    @Override
    public SuccessMessage getAccountHistory(String accountNumber) {

        List<EventStore> events = eventStoreRepository.findAccountHistoryByAccountNumber(accountNumber);
        if (events.isEmpty()) {
            return new SuccessMessage("No events found for this account", null, false, 404);
        }
        List<Object> eventList = new java.util.ArrayList<>();
        for (EventStore event : events) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", event.getEventType());
            eventData.put("eventData", event.getEventData());
            eventData.put("createdAt", event.getCreatedAt().toString());
            eventList.add(eventData);
        }

        return new SuccessMessage("Account history retrieved successfully", eventList, true, 200);
    }

    @Override
    public SuccessMessage replayEvents(String accountNumber) {
        try {
            List<EventStore> events = eventStoreRepository.findAccountHistoryByAccountNumber(accountNumber);
            if (events.isEmpty()) {
                return new SuccessMessage("No events found for this account", null, false, 404);
            }

            BigDecimal balance = BigDecimal.ZERO;
            ObjectMapper mapper = new ObjectMapper();

            for (EventStore event : events) {
                String eventDataJson = event.getEventData(); // JSON stored in DB
                switch (event.getEventType()) {
                    case "AccountCreatedEvent" -> {
                        AccountCreatedEvent createdEvent = mapper.readValue(eventDataJson, AccountCreatedEvent.class);
                        balance = createdEvent.getInitialDeposit();
                        System.out.println("Replayed AccountCreatedEvent: initialDeposit=" + balance);
                    }
                    case "MoneyDepositEvent" -> {
                        MoneyDepositEvent depositEvent = mapper.readValue(eventDataJson, MoneyDepositEvent.class);
                        balance = balance.add(depositEvent.getAmount());
                        System.out.println("Replayed MoneyDepositEvent: amount=" + depositEvent.getAmount());
                    }
                    case "MoneyWithdrawEvent" -> {
                        MoneyDepositEvent withdrawEvent = mapper.readValue(eventDataJson, MoneyDepositEvent.class);
                        balance = balance.subtract(withdrawEvent.getAmount());
                        System.out.println("Replayed MoneyWithdrawEvent: amount=" + withdrawEvent.getAmount());
                    }
                    case "MoneyTransferOutEvent" -> {
                        MoneyTransferOutEvent outEvent = mapper.readValue(eventDataJson, MoneyTransferOutEvent.class);
                        balance = balance.subtract(outEvent.getAmount());
                        System.out.println("Replayed MoneyTransferOutEvent: amount=" + outEvent.getAmount());
                    }
                    case "MoneyTransferInEvent" -> {
                        MoneyTransferInEvent inEvent = mapper.readValue(eventDataJson, MoneyTransferInEvent.class);
                        balance = balance.add(inEvent.getAmount());
                        System.out.println("Replayed MoneyTransferInEvent: amount=" + inEvent.getAmount());
                    }
                    default ->
                        System.out.println("Unknown event type: " + event.getEventType());
                }
            }

            return new SuccessMessage("Events replayed successfully", balance, true, 200);

        } catch (Exception e) {
            e.printStackTrace();
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

}
