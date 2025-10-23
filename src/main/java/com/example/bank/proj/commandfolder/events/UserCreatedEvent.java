package com.example.bank.proj.commandfolder.events;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreatedEvent {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private String createdAt = LocalDateTime.now().toString();
}
