package com.example.bank.proj.commandfolder.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessMessage {
    private String message;
    private Object data;
    private Boolean success;
    private int statusCode;

}
