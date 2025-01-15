package com.chat.chat_room.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.123.62:8000/"; // 本地测试用
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        // 创建一个允许 null 值的 Gson 实例
        Gson gson = new GsonBuilder()
                .serializeNulls()  // 序列化 null 值
                .setLenient()      // 宽松解析
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }
}