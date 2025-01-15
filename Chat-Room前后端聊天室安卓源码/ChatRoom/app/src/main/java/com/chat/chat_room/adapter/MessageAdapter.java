package com.chat.chat_room.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.chat_room.R;
import com.chat.chat_room.model.Message;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();
    private int currentUserId;
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getUserId() == currentUserId ? VIEW_TYPE_ME : VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == VIEW_TYPE_ME ?
                R.layout.item_message_me : R.layout.item_message_other;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView contentTextView;
        private TextView timeTextView;
        private static final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm");

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }

        void bind(Message message) {
            contentTextView.setText(message.getContent());
            if (message.getCreatedAt() != null) {
                timeTextView.setText(message.getCreatedAt().format(formatter));
            }
        }
    }
}