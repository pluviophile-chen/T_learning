package com.chat.chat_room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chat.chat_room.adapter.ChatRoomAdapter;
import com.chat.chat_room.api.RetrofitClient;
import com.chat.chat_room.model.ChatRoom;
import com.chat.chat_room.model.ChatRoomCreate;
import com.chat.chat_room.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ChatRoomAdapter.OnChatRoomClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView chatRoomsRecyclerView;
    private ChatRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        // 初始化 SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadChatRooms);

        // 初始化 RecyclerView
        chatRoomsRecyclerView = findViewById(R.id.chatRoomsRecyclerView);
        chatRoomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatRoomAdapter(this);
        chatRoomsRecyclerView.setAdapter(adapter);

        // 设置创建聊天室按钮
        FloatingActionButton fab = findViewById(R.id.createChatRoomFab);
        fab.setOnClickListener(v -> showCreateChatRoomDialog());

        // 加载聊天室列表
        loadChatRooms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showCreateChatRoomDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_chat_room, null);
        TextInputLayout nameLayout = dialogView.findViewById(R.id.nameLayout);
        TextInputEditText nameEditText = dialogView.findViewById(R.id.nameEditText);

        new MaterialAlertDialogBuilder(this)
                .setTitle("创建聊天室")
                .setView(dialogView)
                .setPositiveButton("创建", (dialog, which) -> {
                    String roomName = nameEditText.getText().toString().trim();
                    if (!roomName.isEmpty()) {
                        createChatRoom(roomName);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadChatRooms() {
        String token = getStoredToken();
        if (token == null) {
            logout();
            return;
        }

        RetrofitClient.getInstance()
                .getApi()
                .getChatRooms("Bearer " + token)
                .enqueue(new Callback<List<ChatRoom>>() {
                    @Override
                    public void onResponse(Call<List<ChatRoom>> call, Response<List<ChatRoom>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setChatRooms(response.body());
                            // 修改这里，使用正确的方法引用
                            adapter.setOnChatRoomDeleteListener(MainActivity.this::showDeleteConfirmationDialog);
                            getCurrentUser();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "Unknown error";
                                Toast.makeText(MainActivity.this,
                                        "加载失败: " + errorBody,
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,
                                        "加载失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatRoom>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "网络错误: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 将 showDeleteConfirmationDialog 方法移到类级别
    private void showDeleteConfirmationDialog(ChatRoom chatRoom) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除聊天室")
                .setMessage("确定要删除这个聊天室吗？此操作不可撤销。")
                .setPositiveButton("删除", (dialog, which) -> deleteChatRoom(chatRoom.getId()))
                .setNegativeButton("取消", null)
                .show();
    }

    private void createChatRoom(String name) {
        String token = getStoredToken();
        if (token == null) {
            logout();
            return;
        }

        ChatRoomCreate request = new ChatRoomCreate(name);
        RetrofitClient.getInstance()
                .getApi()
                .createChatRoom("Bearer " + token, request)
                .enqueue(new Callback<ChatRoom>() {
                    @Override
                    public void onResponse(Call<ChatRoom> call, Response<ChatRoom> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(MainActivity.this,
                                    "聊天室创建成功",
                                    Toast.LENGTH_SHORT).show();
                            loadChatRooms();
                        } else {
                            try {
                                // 打印详细错误信息
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "Unknown error";
                                Toast.makeText(MainActivity.this,
                                        "创建失败: " + errorBody,
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,
                                        "创建失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatRoom> call, Throwable t) {
                        t.printStackTrace(); // 打印堆栈信息
                        Toast.makeText(MainActivity.this,
                                "网络错误: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onChatRoomClick(ChatRoom chatRoom) {
        // 跳转到聊天室详情页面
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("roomId", chatRoom.getId());
        intent.putExtra("roomName", chatRoom.getName());
        startActivity(intent);
    }

    private String getStoredToken() {
        SharedPreferences prefs = getSharedPreferences("ChatApp", MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    private void logout() {
        // 清除存储的token
        SharedPreferences prefs = getSharedPreferences("ChatApp", MODE_PRIVATE);
        prefs.edit().remove("token").apply();

        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void getCurrentUser() {
        String token = getStoredToken();
        if (token == null) {
            logout();
            return;
        }

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
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "获取用户信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void deleteChatRoom(int roomId) {
        String token = getStoredToken();
        if (token == null) {
            logout();
            return;
        }

        RetrofitClient.getInstance()
                .getApi()
                .deleteChatRoom("Bearer " + token, roomId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "聊天室已删除",
                                    Toast.LENGTH_SHORT).show();
                            loadChatRooms(); // 重新加载列表
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "删除失败";
                                Toast.makeText(MainActivity.this,
                                        errorBody,
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,
                                        "删除失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "网络错误: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}