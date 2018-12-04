package com.videocall.tsfchat.ui.profile;

import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.data.repository.UserRepository;

/**
 * Created on : May 16, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class ProfilePresenter {
    private View view;
    private UserRepository userRepository;

    public ProfilePresenter(View view, UserRepository userRepository) {
        this.view = view;
        this.userRepository = userRepository;
    }

    public void loadUser() {
        userRepository.getCurrentUser(
                view::showUser,
                throwable -> {
                });
    }

    public void logout() {
        userRepository.logout();
        view.showLoginPage();
    }

    public void uploadPhoto(String realPathFromURI) {
        userRepository.uploadPhoto(realPathFromURI, total -> {
                },
                user -> view.showUser(user),
                throwable -> view.showError(throwable.getMessage()));
    }

    public interface View {
        void showUser(User user);

        void showLoginPage();

        void showError(String message);
    }
}
