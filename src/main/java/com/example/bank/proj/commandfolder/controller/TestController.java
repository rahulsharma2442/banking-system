package com.example.bank.proj.commandfolder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.bank.proj.sharedfolder.publisher.RedisPublisher;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisPublisher publisher;

    @GetMapping("/send")
    public String sendMessage() {
        publisher.publish("Hello Redis!");
        return "Message sent!";
    }
}