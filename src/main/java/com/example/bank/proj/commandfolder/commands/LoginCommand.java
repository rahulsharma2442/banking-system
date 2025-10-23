package com.example.bank.proj.commandfolder.commands;

import lombok.Data; 
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginCommand {
    private String username;
    private String password;
}
