package com.example.bank.proj.sharedfolder.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bank.proj.commandfolder.entites.User;

@Repository
public interface UserDetailsRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

}