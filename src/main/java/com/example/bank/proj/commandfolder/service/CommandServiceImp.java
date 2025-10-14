package com.example.bank.proj.commandfolder.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public void createAccount(CreateAccountCommand command) {
        User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountType(command.getAccountType());
        account.setBalance(command.getInitialDeposit());
        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        String accountNumber = String.format("%08d", accountRepository.count() + 1);
        account.setAccountNumber(accountNumber);

        account = accountRepository.save(account);

        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getId(),
                account.getUser().getId(),
                account.getAccountType(),
                account.getBalance(),
                account.getCreatedAt()
        );

        // Save event in EventStore
        EventStore eventStore = new EventStore();
        eventStore.setEventType("AccountCreatedEvent");
        eventStore.setEventData(event.toString());
        eventStore.setCreatedAt(LocalDateTime.now());
        eventStore.setAggregateId(account.getId() + "" + account.getUser().getId());
        eventStore.setAggregateType("Account");
        eventStoreRepository.save(eventStore);

        // ✅ Publish to Redis
        eventPublisher.publishAccountEvent("accountEvents", event);
    }

    @Override
    public void userRegistration(UserRegisterCommand command) {
        User user = new User();
        user.setName(command.getUsername());
        user.setPassword(command.getPassword());
        user.setEmail(command.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("USER");
        user.setEnabled(true);

        userRepository.save(user);
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
            eventStore.setAggregateId(account.getId() + "" + account.getUser().getId());
            eventStore.setAggregateType("Account");
            eventStoreRepository.save(eventStore);
            eventPublisher.publishAccountEvent("depositEvents", event);

            return new SuccessMessage("Amount deposited successfully", account, true, 200);

        } catch (Exception e) {
            return new SuccessMessage("Error occurred: " + e.getMessage(), null, false, 500);
        }
    }

}
