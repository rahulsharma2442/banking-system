package com.example.bank.proj.queryfolder.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.bank.proj.queryfolder.readmodel.AccountReadModel;
@Repository
public interface AccountReadRepository extends MongoRepository<AccountReadModel, String> {
    boolean existsById(String id);
    Optional<AccountReadModel> findById(String id);

    boolean existsByMySqlId(Long mySqlId);
    Optional<AccountReadModel> findByMySqlId(Long mySqlId);

    Optional<AccountReadModel> findByAccountNumber(String accountNumber);

}
