package com.example.bank.proj.commandfolder.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.proj.commandfolder.commands.UserRegisterCommand;
import com.example.bank.proj.commandfolder.entites.User;
import com.example.bank.proj.commandfolder.repositorioes.UserRepository;
import com.example.bank.proj.commandfolder.service.CommandService;


@RestController
@RequestMapping("/users")
public class UserController {

    private final CommandService commandService;
    private final UserRepository userRepository;

    public UserController(CommandService commandService, UserRepository userRepository) {
        this.commandService = commandService;
        this.userRepository = userRepository;
    }
    @GetMapping("/user")
    public User getUser(@RequestParam Long id) {
        return userRepository.findById(id).orElse(null);
    }
    @GetMapping("/allUsers")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // @PostMapping("/authenticate/register")
    // public String userRegistration(@RequestBody UserRegisterCommand command) {
    //     try {
    //         commandService.userRegistration(command);
    //         return "User registered successfully!";
    //     } catch (Exception e) {
    //         return "Error registering user: " + e.getMessage();
    //     }
    // }
}
