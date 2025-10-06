package com.example.bank.proj.queryfolder.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.bank.proj.queryfolder.readmodel.AccountReadModel;
@Repository
public interface AccountReadRepository extends MongoRepository<AccountReadModel, Long> {
    boolean existsById(Long accountId);

    
}
