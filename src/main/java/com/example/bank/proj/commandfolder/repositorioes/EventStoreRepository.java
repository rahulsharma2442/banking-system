package com.example.bank.proj.commandfolder.repositorioes;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.bank.proj.commandfolder.entites.EventStore;


@Repository
public interface  EventStoreRepository extends JpaRepository<EventStore, Long> {
    List<EventStore> findByAggregateId(String aggregateId);
    List<EventStore> findByAggregateIdAndEventType(String aggregateId, String eventType);
    List<EventStore> findByAggregateIdAndAggregateType(String aggregateId, String aggregateType);

    @Query("SELECT e FROM EventStore e WHERE e.aggregateId = ?1 AND e.eventType IN ( 'MoneyDepositEvent', 'MoneyWithdrawEvent', 'MoneyTransferOutEvent','MoneyTransferInEvent','AccountCreatedEvent')")
    List<EventStore> findAccountHistoryByAccountNumber(String accountNumber);
}
  