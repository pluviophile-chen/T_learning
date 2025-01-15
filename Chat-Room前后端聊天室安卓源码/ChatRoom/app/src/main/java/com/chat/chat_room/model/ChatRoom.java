package com.chat.chat_room.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class ChatRoom {
    private int id;
    private String name;
    @SerializedName("creator_id")
    private int creatorId;

    @SerializedName("created_at")
    private LocalDateTime createdAt;
    public ChatRoom(int id, String name, int creatorId) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}