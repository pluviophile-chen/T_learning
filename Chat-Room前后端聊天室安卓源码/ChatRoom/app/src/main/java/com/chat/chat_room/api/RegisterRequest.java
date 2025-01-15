package com.chat.chat_room.api;

public class RegisterRequest {
    private String username;
    private String password;

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}