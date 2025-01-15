package com.chat.chat_room.api;

public class LoginResponse {
    private String access_token;
    private String token_type;

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }
}