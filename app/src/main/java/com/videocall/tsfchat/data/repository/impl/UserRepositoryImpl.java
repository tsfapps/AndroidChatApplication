package com.videocall.tsfchat.data.repository.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.data.repository.UserRepository;
import com.videocall.tsfchat.data.repository.remote.ContactRestApi;
import com.videocall.tsfchat.util.Action;
import com.videocall.tsfchat.util.AvatarUtil;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : January 30, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class UserRepositoryImpl implements UserRepository {

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public UserRepositoryImpl(Context context) {
        sharedPreferences = context.getSharedPreferences("users", Context.MODE_PRIVATE);
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void login(String name, String email, String password, Action<User> onSuccess, Action<Throwable> onError) {
        Qiscus.setUser(email, password)
                .withUsername(name)
                .withAvatarUrl(AvatarUtil.generateAvatar(name))
                .save()
                .map(this::mapFromQiscusAccount)
                .doOnNext(this::setCurrentUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void getCurrentUser(Action<User> onSuccess, Action<Throwable> onError) {
        getCurrentUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void getUsers(Action<List<User>> onSuccess, Action<Throwable> onError) {
        getUsersObservable()
                .startWith(getLocalUsers())
                .doOnNext(this::saveLocalUsers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void updateProfile(String name, Action<User> onSuccess, Action<Throwable> onError) {
        Qiscus.updateUserAsObservable(name, getCurrentUser().getAvatarUrl())
                .map(this::mapFromQiscusAccount)
                .doOnNext(this::setCurrentUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void logout() {
        Qiscus.clearUser();
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public void uploadPhoto(String realPathFromURI, QiscusApi.ProgressListener progressListener,
                            Action<User> onSuccess, Action<Throwable> onError) {
        QiscusApi.getInstance()
                .uploadFile(new File(realPathFromURI), progressListener)
                .flatMap(uri -> Qiscus.updateUserAsObservable(getCurrentUser().getName(), uri.toString()))
                .map(this::mapFromQiscusAccount)
                .doOnNext(this::setCurrentUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    private Observable<User> getCurrentUserObservable() {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(getCurrentUser());
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    private User getCurrentUser() {
        return gson.fromJson(sharedPreferences.getString("current_user", ""), User.class);
    }

    private void setCurrentUser(User user) {
        sharedPreferences.edit()
                .putString("current_user", gson.toJson(user))
                .apply();
    }

    private Observable<List<User>> getUsersObservable() {
        return ContactRestApi.INSTANCE.getContacts()
                .toObservable()
                .flatMap(jsonObject -> Observable.from(jsonObject.get("results").getAsJsonObject().get("users").getAsJsonArray()))
                .map(jsonElement -> {
                    User user = new User();
                    user.setId(jsonElement.getAsJsonObject().get("email").getAsString());
                    user.setName(jsonElement.getAsJsonObject().get("name").getAsString());
                    user.setAvatarUrl(jsonElement.getAsJsonObject().get("avatar_url").getAsString());
                    return user;
                })
                .filter(user -> !user.equals(getCurrentUser()))
                .toList();
    }

    private User mapFromQiscusAccount(QiscusAccount qiscusAccount) {
        User user = new User();
        user.setId(qiscusAccount.getEmail());
        user.setName(qiscusAccount.getUsername());
        user.setAvatarUrl(qiscusAccount.getAvatar());
        return user;
    }

    private void saveLocalUsers(List<User> users) {
        sharedPreferences.edit()
                .putString("users", gson.toJson(users))
                .apply();
    }

    private List<User> getLocalUsers() {
        List<User> users = gson.fromJson(sharedPreferences.getString("users", ""),
                new TypeToken<List<User>>() {
                }.getType());

        if (users == null) {
            return new ArrayList<>();
        }

        return users;
    }
}
