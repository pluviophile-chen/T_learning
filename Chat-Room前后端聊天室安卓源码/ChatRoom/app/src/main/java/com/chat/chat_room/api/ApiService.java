package com.chat.chat_room.api;

import com.chat.chat_room.model.ChatRoom;
import com.chat.chat_room.model.ChatRoomCreate;
import com.chat.chat_room.model.Message;
import com.chat.chat_room.model.MessageCreate;
import com.chat.chat_room.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("chatrooms")
    Call<List<ChatRoom>> getChatRooms(@Header("Authorization") String token);

    @POST("chatrooms")
    Call<ChatRoom> createChatRoom(@Header("Authorization") String token, @Body ChatRoomCreate chatRoom);

    @FormUrlEncoded
    @POST("token")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // 添加删除聊天室的接口
    @DELETE("chatrooms/{roomId}")
    Call<Void> deleteChatRoom(
            @Header("Authorization") String token,
            @Path("roomId") int roomId
    );

    // 获取当前用户信息的接口
    @GET("users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    @GET("chatrooms/{roomId}/messages")
    Call<List<Message>> getMessages(
            @Header("Authorization") String token,
            @Path("roomId") int roomId
    );

    @POST("chatrooms/{roomId}/messages")
    Call<Message> createMessage(
            @Header("Authorization") String token,
            @Path("roomId") int roomId,
            @Body MessageCreate message
    );
}