package com.videocall.tsfchat.ui.addmember;

import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.data.repository.ChatRoomRepository;
import com.videocall.tsfchat.data.repository.UserRepository;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AddGroupMemberPresenter {
    private View view;
    private UserRepository userRepository;
    private ChatRoomRepository chatRoomRepository;
    private List<User> contacts;
    private List<QiscusRoomMember> members;

    public AddGroupMemberPresenter(View view,
                                   UserRepository userRepository,
                                   ChatRoomRepository chatRoomRepository,
                                   List<QiscusRoomMember> members) {
        this.view = view;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.members = members;
        contacts = new ArrayList<>();
    }

    public void loadContacts() {
        userRepository.getUsers(users -> {
                    Observable.from(users)
                            .filter(user -> {
                                QiscusRoomMember member = new QiscusRoomMember();
                                member.setEmail(user.getId());
                                return !members.contains(member);
                            })
                            .toList()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(newUsers -> {
                                        contacts = newUsers;
                                        view.showContacts(contacts);
                                    },
                                    throwable -> view.showErrorMessage(throwable.getMessage()));
                },
                throwable -> view.showErrorMessage(throwable.getMessage())
        );
    }

    public void search(String keyword) {
        Observable.from(contacts)
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> view.showContacts(users), throwable -> view.showErrorMessage(throwable.getMessage()));

    }

    public void addMember(long roomId, User user) {
        view.showLoading();
        chatRoomRepository.addMember(roomId, user, aVoid -> {
            view.onMemberAdded(user);
            view.dismissLoading();
        }, throwable -> {
            view.showErrorMessage(throwable.getMessage());
            view.dismissLoading();
        });
    }

    public interface View {

        void showContacts(List<User> contacts);

        void onMemberAdded(User user);

        void showLoading();

        void dismissLoading();

        void showErrorMessage(String errorMessage);
    }
}
