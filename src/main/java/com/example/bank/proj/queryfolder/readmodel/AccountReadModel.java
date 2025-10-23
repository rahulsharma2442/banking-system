package com.example.bank.proj.queryfolder.readmodel;

import java.math.BigDecimal;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountReadModel {
    @Id
    private String id;
    private Long mySqlId;
    private String accountNumber;
    private Long userId;
    private String accountType;
    private BigDecimal balance;
    private String createdAt;
    private String updatedAt;
}
