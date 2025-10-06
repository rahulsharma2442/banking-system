package com.example.bank.proj.commandfolder.repositorioes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bank.proj.commandfolder.entites.EventStore;

@Repository
public interface  EventStoreRepository extends JpaRepository<EventStore, Long> {
    
}
  