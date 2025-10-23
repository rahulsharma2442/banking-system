package com.example.bank.proj.commandfolder.events;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountCreatedEvent {
    private String accountNumber;
    private Long userId;
    private String accountType;
    private BigDecimal initialDeposit; // Using BigDecimal to avoid floating-point issues
    private String timestamp;
    
}
