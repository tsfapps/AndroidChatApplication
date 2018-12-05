package com.videocall.tsfchat.data.repository;


import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.util.Action;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.util.List;

public interface UserRepository {

    void login(String name, String email, String password, Action<User> onSuccess, Action<Throwable> onError);

    void getCurrentUser(Action<User> onSuccess, Action<Throwable> onError);

    void getUsers(Action<List<User>> onSuccess, Action<Throwable> onError);

    void updateProfile(String name, Action<User> onSuccess, Action<Throwable> onError);

    void logout();

    void uploadPhoto(String realPathFromURI, QiscusApi.ProgressListener progressListener,
                     Action<User> onSuccess, Action<Throwable> onError);
}
