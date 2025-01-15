package com.chat.chat_room.model;

public class MessageCreate {
    private String content;

    public MessageCreate(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}