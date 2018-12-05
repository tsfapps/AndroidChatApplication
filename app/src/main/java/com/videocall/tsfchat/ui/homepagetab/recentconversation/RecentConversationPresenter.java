package com.videocall.tsfchat.ui.homepagetab.recentconversation;

import com.videocall.tsfchat.data.repository.ChatRoomRepository;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;



public class RecentConversationPresenter {
    private View view;
    private ChatRoomRepository chatRoomRepository;

    public RecentConversationPresenter(View view, ChatRoomRepository chatRoomRepository) {
        this.view = view;
        this.chatRoomRepository = chatRoomRepository;

        EventBus.getDefault().register(this);
    }

    public void loadChatRooms() {
        chatRoomRepository.getChatRooms(view::showChatRooms,
                throwable -> view.showErrorMessage(throwable.getMessage()));
    }

    public void openChatRoom(QiscusChatRoom chatRoom) {
        if (chatRoom.isGroup()) {
            view.showGroupChatRoomPage(chatRoom);
            return;
        }
        view.showChatRoomPage(chatRoom);
    }

    @Subscribe
    public void onReceivedComment(QiscusCommentReceivedEvent event) {
        chatRoomRepository.getChatRoom(event.getQiscusComment().getRoomId(),
                qiscusChatRoom -> {
                    view.onChatRoomUpdated(qiscusChatRoom);
                }, Throwable::printStackTrace);
    }

    public void detachView() {
        EventBus.getDefault().unregister(this);
    }

    public interface View {
        void showChatRooms(List<QiscusChatRoom> chatRooms);

        void onChatRoomUpdated(QiscusChatRoom chatRoom);

        void showChatRoomPage(QiscusChatRoom chatRoom);

        void showGroupChatRoomPage(QiscusChatRoom chatRoom);

        void showErrorMessage(String errorMessage);
    }
}
