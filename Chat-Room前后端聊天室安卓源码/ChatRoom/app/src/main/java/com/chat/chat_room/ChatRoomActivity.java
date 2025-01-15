package com.chat.chat_room;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chat.chat_room.adapter.MessageAdapter;
import com.chat.chat_room.api.RetrofitClient;
import com.chat.chat_room.model.Message;
import com.chat.chat_room.model.MessageCreate;
import com.chat.chat_room.model.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter adapter;
    private TextInputEditText messageEditText;
    private int roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // 获取聊天室信息
        roomId = getIntent().getIntExtra("roomId", -1);
        String roomName = getIntent().getStringExtra("roomName");

        // 设置工具栏
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(roomName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化视图
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);

        // 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter();
        messagesRecyclerView.setAdapter(adapter);

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadMessages);

        // 设置发送按钮
        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());

        // 加载消息
        loadMessages();
        getCurrentUser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMessages() {
        String token = getStoredToken();
        if (token == null || roomId == -1) return;

        RetrofitClient.getInstance()
                .getApi()
                .getMessages("Bearer " + token, roomId)
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setMessages(response.body());
                            messagesRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
                        } else {
                            Toast.makeText(ChatRoomActivity.this,
                                    "加载消息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(ChatRoomActivity.this,
                                "网络错误",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage() {
        String content = messageEditText.getText().toString().trim();
        if (content.isEmpty()) return;

        String token = getStoredToken();
        if (token == null || roomId == -1) return;

        // 创建消息请求对象
        MessageCreate request = new MessageCreate(content);

        RetrofitClient.getInstance()
                .getApi()
                .createMessage("Bearer " + token, roomId, request)
                .enqueue(new Callback<Message>() {
                    @Override
                    public void onResponse(Call<Message> call, Response<Message> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            messageEditText.setText("");
                            adapter.addMessage(response.body());
                            messagesRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
                        } else {
                            Toast.makeText(ChatRoomActivity.this,
                                    "发送失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Message> call, Throwable t) {
                        Toast.makeText(ChatRoomActivity.this,
                                "网络错误",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getCurrentUser() {
        String token = getStoredToken();
        if (token == null) return;

        RetrofitClient.getInstance()
                .getApi()
                .getCurrentUser("Bearer " + token)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setCurrentUserId(response.body().getId());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(ChatRoomActivity.this,
                                "获取用户信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getStoredToken() {
        return getSharedPreferences("ChatApp", MODE_PRIVATE)
                .getString("token", null);
    }
}