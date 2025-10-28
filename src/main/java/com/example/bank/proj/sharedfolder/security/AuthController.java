package com.example.bank.proj.sharedfolder.security;

import java.util.HashMap;
import java.util.Map;

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
import com.example.bank.proj.commandfolder.dto.SuccessMessage;
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
    public ResponseEntity<?> generateToken(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate the user
            User user = userRepository.findByEmail(authRequest.getEmail());
            if (user == null) {
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
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterCommand command) {
        System.out.println("Registering user: " + command.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(command.getEmail()) != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already exists"));
        }

        try {
            // 1️⃣ Save the user
            SuccessMessage result = commandService.userRegistration(command);
            if (!result.getSuccess()) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", result.getMessage()));
            }

            // 2️⃣ Fetch the newly created user
            User user = userRepository.findByEmail(command.getEmail());

            // 3️⃣ Generate token for the new user
            String token = jwtUtil.generateToken(user.getUsername());

            // 4️⃣ Return token + basic user info
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error registering user: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1️⃣ Check if user exists
            User user = userRepository.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found"));
            }

            // 2️⃣ Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid email or password"));
            }

            // 3️⃣ Authenticate the user (Spring Security)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );

            // 4️⃣ Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername());

            // 5️⃣ Return token + basic user info
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error during login: " + e.getMessage()));
        }
    }

}
