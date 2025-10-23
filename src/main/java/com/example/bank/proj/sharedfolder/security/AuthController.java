package com.example.bank.proj.sharedfolder.security;


import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
import com.example.bank.proj.commandfolder.entites.User;
import com.example.bank.proj.commandfolder.repositorioes.UserRepository;
import com.example.bank.proj.commandfolder.service.CommandService;


@RestController
@RequestMapping("/authenticate")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommandService commandService;

   

    @PostMapping("/auth")
    public ResponseEntity<?> generateToken(@RequestBody AuthRequest authRequest){
        try{
            // Authenticate the user
            User user = userRepository.findByEmail(authRequest.getEmail());
            if(user==null){
                System.out.println("User is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: User not found");
            }                   
                // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: User not found");
            
            // System.out.println("Authenticating user: " + authRequest.getEmail());

            if (user == null || !passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            String userName = user.getUsername();
            authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userName, authRequest.getPassword()));
            // If authentication is successful, generate and return the JWT token
            // The JWTUtil class should have a method to generate the token
            String token = jwtUtil.generateToken(userName);
            System.out.println("Generated token for user: " + authRequest.getEmail());
            HashMap<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
        
    }
     @PostMapping("/register")
    public String register(@RequestBody UserRegisterCommand command ) {
        System.out.println("Registering user: " + command.getEmail());
        if (userRepository.findByEmail(command.getEmail()) != null) {
            return "Email Already Exist";
        }
          try {
            commandService.userRegistration(command);
            return "User registered successfully!";
        } catch (Exception e) {
            return "Error registering user: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail());
            String userName = user.getUsername();
            if (user == null || !passwordEncoder.matches(userName, user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userName , request.getPassword()));
                    String token = jwtUtil.generateToken(userName);
        return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}