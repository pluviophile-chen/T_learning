package com.chat.chat_room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chat.chat_room.api.LoginResponse;
import com.chat.chat_room.api.RetrofitClient;
import com.chat.chat_room.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        // 检查是否已经登录并验证token
        validateTokenAndAutoLogin();

        // 设置点击事件
        loginButton.setOnClickListener(v -> attemptLogin());
        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void validateTokenAndAutoLogin() {
        String token = getStoredToken();
        if (token != null) {
            // 验证 token 有效性
            RetrofitClient.getInstance()
                    .getApi()
                    .getCurrentUser("Bearer " + token)
                    .enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // token 有效，直接进入主界面
                                startMainActivity();
                                finish();
                            } else {
                                // token 无效，清除并要求重新登录
                                clearStoredToken();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            // 网络错误，保持在登录界面
                            Toast.makeText(LoginActivity.this,
                                    "网络连接失败，请重试",
                                    Toast.LENGTH_SHORT).show();
                            clearStoredToken();
                        }
                    });
        }
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // 输入验证
        if (username.isEmpty()) {
            usernameEditText.setError("请输入用户名");
            usernameEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("请输入密码");
            passwordEditText.requestFocus();
            return;
        }

        // 显示加载进度
        loginButton.setEnabled(false);
        loginButton.setText("登录中...");

        // 调用登录API
        RetrofitClient.getInstance()
                .getApi()
                .login(username, password)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        loginButton.setEnabled(true);
                        loginButton.setText("登录");

                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().getAccessToken();
                            if (token != null && !token.isEmpty()) {
                                saveToken(token);
                                startMainActivity();
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "登录失败：无效的token",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                // 解析错误信息
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "未知错误";
                                if (response.code() == 400) {
                                    Toast.makeText(LoginActivity.this,
                                            "用户名或密码错误",
                                            Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 401) {
                                    Toast.makeText(LoginActivity.this,
                                            "认证失败",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "登录失败: " + errorBody,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this,
                                        "登录失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        loginButton.setEnabled(true);
                        loginButton.setText("登录");
                        Toast.makeText(LoginActivity.this,
                                "网络错误: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("ChatApp", MODE_PRIVATE);
        prefs.edit()
                .putString("token", token)
                .putLong("token_timestamp", System.currentTimeMillis())
                .apply();
    }

    private String getStoredToken() {
        SharedPreferences prefs = getSharedPreferences("ChatApp", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        long timestamp = prefs.getLong("token_timestamp", 0);

        // 检查 token 是否过期（这里假设 token 有效期为 30 天）
        if (token != null && System.currentTimeMillis() - timestamp > 30 * 24 * 60 * 60 * 1000L) {
            clearStoredToken();
            return null;
        }
        return token;
    }

    private void clearStoredToken() {
        SharedPreferences prefs = getSharedPreferences("ChatApp", MODE_PRIVATE);
        prefs.edit()
                .remove("token")
                .remove("token_timestamp")
                .apply();
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}