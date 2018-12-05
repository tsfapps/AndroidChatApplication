package com.videocall.tsfchat.ui.addmember;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.videocall.tsfchat.AppComponent;
import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.util.List;


public class AddGroupMemberActivity extends AppCompatActivity implements AddGroupMemberPresenter.View {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected QiscusChatRoom qiscusChatRoom;

    private ContactAdapter contactAdapter;
    private SearchView searchView;
    private ProgressDialog progressDialog;

    private AddGroupMemberPresenter presenter;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        Intent intent = new Intent(context, AddGroupMemberActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_member);
        resolveChatRoom(savedInstanceState);

        if (qiscusChatRoom == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setVisibility(View.GONE);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setTitle("Add Participants");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait!");

        RecyclerView contactRecyclerView = findViewById(R.id.recyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setHasFixedSize(true);

        contactAdapter = new ContactAdapter(this, position -> {
            addMember(contactAdapter.getData().get(position));
        });
        contactRecyclerView.setAdapter(contactAdapter);

        AppComponent appComponent = SampleApp.getInstance().getComponent();
        presenter = new AddGroupMemberPresenter(this, appComponent.getUserRepository(),
                appComponent.getChatRoomRepository(), qiscusChatRoom.getMember());
        presenter.loadContacts();
    }

    private void addMember(User user) {
        new AlertDialog.Builder(this)
                .setTitle(user.getName())
                .setMessage("Are you sure want to add " + user.getName())
                .setPositiveButton("Add", (dialog, which) -> {
                    presenter.addMember(qiscusChatRoom.getId(), user);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .create()
                .show();
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.toString().toLowerCase();
                presenter.search(query);
                searchView.clearFocus();
                return true;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toString().toLowerCase();
                presenter.search(newText);
                return true;
            }

        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_search) {
            onBackPressed();
        }

        return true;
    }

    @Override
    public void showContacts(List<User> contacts) {
        contactAdapter.clear();
        contactAdapter.add(contacts);
    }

    @Override
    public void onMemberAdded(User user) {
        QiscusRoomMember member = new QiscusRoomMember();
        member.setEmail(user.getId());
        member.setAvatar(user.getAvatarUrl());
        member.setUsername(user.getName());
        qiscusChatRoom.getMember().add(member);

        Intent data = new Intent();
        data.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        setResult(Activity.RESULT_OK, data);
        finish();
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
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
