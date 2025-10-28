package com.example.bank.proj.commandfolder.repositorioes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import com.example.bank.proj.commandfolder.entites.Account;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") // 5 sec timeout
    })
    @Query("SELECT a FROM Account a WHERE a.accountNumber=:accountNumber")
    Account findAccountForUpdate(@Param("accountNumber") String accountNumber);

    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);
}
