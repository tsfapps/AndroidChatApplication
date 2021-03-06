package com.videocall.tsfchat;

import android.content.Context;

import com.videocall.tsfchat.data.repository.ChatRoomRepository;
import com.videocall.tsfchat.data.repository.UserRepository;
import com.videocall.tsfchat.data.repository.impl.ChatRoomRepositoryImpl;
import com.videocall.tsfchat.data.repository.impl.UserRepositoryImpl;

public class AppComponent {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    AppComponent(Context context){
        userRepository = new UserRepositoryImpl(context);
        chatRoomRepository = new ChatRoomRepositoryImpl();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public ChatRoomRepository getChatRoomRepository() {
        return chatRoomRepository;
    }
}
