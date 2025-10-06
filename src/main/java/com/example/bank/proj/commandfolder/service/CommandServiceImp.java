package com.example.bank.proj.commandfolder.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bank.proj.commandfolder.commands.CreateAccountCommand;
import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
import com.example.bank.proj.commandfolder.entites.Account;
import com.example.bank.proj.commandfolder.entites.EventStore;
import com.example.bank.proj.commandfolder.entites.User;
import com.example.bank.proj.commandfolder.events.AccountCreatedEvent;
import com.example.bank.proj.commandfolder.repositorioes.AccountRepository;
import com.example.bank.proj.commandfolder.repositorioes.EventStoreRepository;
import com.example.bank.proj.commandfolder.repositorioes.UserRepository;
import com.example.bank.proj.sharedfolder.publisher.AccountEventPublisher;


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
        eventPublisher.publishAccountEvent( event);
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
}
