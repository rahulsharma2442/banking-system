package com.example.bank.proj.queryfolder.service;

import java.util.List;
import com.example.bank.proj.queryfolder.readmodel.AccountReadModel;

public interface AccountReadService {

    void processAccountCreatedEvent(String message);
    List<AccountReadModel> getAllAccounts();
}