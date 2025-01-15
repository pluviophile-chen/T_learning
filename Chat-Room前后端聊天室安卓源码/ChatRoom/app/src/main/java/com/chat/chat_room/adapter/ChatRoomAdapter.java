package com.chat.chat_room.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.chat_room.R;
import com.chat.chat_room.model.ChatRoom;


import java.util.ArrayList;
import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    private List<ChatRoom> chatRooms = new ArrayList<>();
    private OnChatRoomClickListener listener;
    private OnChatRoomDeleteListener deleteListener;
    private int currentUserId;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public interface OnChatRoomDeleteListener {
        void onDeleteClick(ChatRoom chatRoom);
    }

    public ChatRoomAdapter(OnChatRoomClickListener listener) {
        this.listener = listener;
    }

    public void setOnChatRoomDeleteListener(OnChatRoomDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("当前用户ID: " + userId); // 添加日志
        notifyDataSetChanged();
    }

    public void setChatRooms(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.bind(chatRoom);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private TextView roomNameTextView;
        private TextView creatorNameTextView;
        private ImageButton deleteButton;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTextView = itemView.findViewById(R.id.roomNameTextView);
            creatorNameTextView = itemView.findViewById(R.id.creatorNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChatRoomClick(chatRooms.get(position));
                }
            });
        }

        void bind(ChatRoom chatRoom) {
            roomNameTextView.setText(chatRoom.getName());
            creatorNameTextView.setText("聊天室 #" + chatRoom.getId());

            System.out.println("聊天室创建者ID: " + chatRoom.getCreatorId()); // 添加日志
            System.out.println("当前用户ID: " + currentUserId); // 添加日志
            // 只有创建者才能看到删除按钮
            if (chatRoom.getCreatorId() == currentUserId) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(chatRoom);
                    }
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }
        }
    }
}