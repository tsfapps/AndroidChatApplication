package com.videocall.tsfchat.ui.homepagetab.recentconversation;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.ui.groupchatroom.GroupChatRoomActivity;
import com.videocall.tsfchat.ui.privatechatcreation.PrivateChatCreationActivity;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.ui.QiscusChatActivity;

import java.util.List;

/**
 * Created by asyrof on 17/11/17.
 */
public class RecentConversationFragment extends Fragment implements RecentConversationPresenter.View, SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyRoomView;

    private RecentConversationPresenter presenter;
    private RecentConversationAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_conversation, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();
        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        FloatingActionButton fabCreateNewConversation = v.findViewById(R.id.buttonCreateNewConversation);
        fabCreateNewConversation.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PrivateChatCreationActivity.class);
            startActivity(intent);
        });

        emptyRoomView = v.findViewById(R.id.empty_room_view);
        adapter = new RecentConversationAdapter(getActivity(), position -> {
            presenter.openChatRoom(adapter.getData().get(position));
        });

        RecyclerView recyclerView = v.findViewById(R.id.recyclerRecentConversation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        presenter = new RecentConversationPresenter(this, SampleApp.getInstance().getComponent().getChatRoomRepository());
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadChatRooms();
    }

    @Override
    public void showChatRooms(List<QiscusChatRoom> chatRooms) {
        adapter.addOrUpdate(chatRooms);
        emptyRoomView.setVisibility(adapter.getData().size() > 0 ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onChatRoomUpdated(QiscusChatRoom chatRoom) {
        adapter.addOrUpdate(chatRoom);
        emptyRoomView.setVisibility(adapter.getData().size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showChatRoomPage(QiscusChatRoom chatRoom) {
        startActivity(QiscusChatActivity.generateIntent(getActivity(), chatRoom));
    }

    @Override
    public void showGroupChatRoomPage(QiscusChatRoom chatRoom) {
        startActivity(GroupChatRoomActivity.generateIntent(getActivity(), chatRoom));
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        presenter.loadChatRooms();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}