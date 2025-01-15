package com.chat.chat_room.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class Message {
    private int id;
    private String content;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("chatroom_id")
    private int chatroomId;

    @SerializedName("created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getChatroomId() { return chatroomId; }
    public void setChatroomId(int chatroomId) { this.chatroomId = chatroomId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}