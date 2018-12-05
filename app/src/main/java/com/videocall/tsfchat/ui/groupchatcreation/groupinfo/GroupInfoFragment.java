package com.videocall.tsfchat.ui.groupchatcreation.groupinfo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.ui.homepagetab.HomePageTabActivity;
import com.videocall.tsfchat.util.ChatRoomNavigator;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoFragment extends Fragment implements GroupInfoPresenter.View {
    private static final String CONTACT_KEY = "CONTACT_KEY";
    private static final String selectMore = "select at least one";
    private static final String groupNameFormat = "Please input group name";
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private EditText groupNameView;
    private GroupInfoPresenter presenter;
    private List<User> contacts;

    public static GroupInfoFragment newInstance(List<User> contacts) {
        GroupInfoFragment fragment = new GroupInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(CONTACT_KEY, (ArrayList<User>) contacts);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Add Group Info");

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please wait...");

        View view = inflater.inflate(R.layout.fragment_group_info, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewSelected);
        groupNameView = view.findViewById(R.id.group_name_input);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        contacts = getArguments().getParcelableArrayList(CONTACT_KEY);
        if (contacts == null) {
            getActivity().finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        ContactAdapter adapter = new ContactAdapter(getActivity(), null);
        recyclerView.setAdapter(adapter);
        adapter.addOrUpdate(contacts);

        presenter = new GroupInfoPresenter(this, SampleApp.getInstance().getComponent().getChatRoomRepository());
    }

    public void proceedCreateGroup() {
        String groupName = groupNameView.getText().toString();
        boolean groupNameInputted = groupName.trim().length() > 0;
        if (groupNameInputted && selectedContactIsMoreThanOne()) {
            presenter.createGroup(groupName, contacts);
        } else {
            String warningText = (groupNameInputted) ? selectMore : groupNameFormat;
            showErrorMessage(warningText);
        }
    }

    private boolean selectedContactIsMoreThanOne() {
        return this.contacts.size() > 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void showLoading() {
        progressDialog.show();
    }

    @Override
    public void dismissLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void showGroupChatRoomPage(QiscusChatRoom chatRoom) {
        ChatRoomNavigator.openChatRoom(getActivity(), chatRoom)
                .withParentClass(HomePageTabActivity.class)
                .start();
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}


