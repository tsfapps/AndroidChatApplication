package com.videocall.tsfchat.data.repository;

import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.util.Action;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

/**
 * Created on : January 31, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface ChatRoomRepository {
    void getChatRoom(long roomId, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError);

    void getChatRooms(Action<List<QiscusChatRoom>> onSuccess, Action<Throwable> onError);

    void createChatRoom(User user, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError);

    void createGroupChatRoom(String name, List<User> members, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError);

    void addMember(long roomId, User user, Action<Void> onSuccess, Action<Throwable> onError);

    void removeMember(long roomId, User user, Action<Void> onSuccess, Action<Throwable> onError);

    void updateGroupChatRoomName(long roomId, String name, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError);
}
