package com.example.bank.proj.sharedfolder.security;

import lombok.Data;

@Data
public class AuthRequest {
    
    private String email;
    private String password;

}
