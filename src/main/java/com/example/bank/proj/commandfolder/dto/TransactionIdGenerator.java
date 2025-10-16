package com.example.bank.proj.commandfolder.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TransactionIdGenerator {

    private static final Random RANDOM = new Random();

    public static String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomSuffix = String.format("%04X", RANDOM.nextInt(0xFFFF)); // 4-digit hex
        return "TXN_" + timestamp + "_" + randomSuffix;
    }
}
