package com.videocall.tsfchat.ui.privatechatcreation;

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

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.ui.groupchatcreation.GroupChatCreationActivity;

import java.util.List;


public class PrivateChatCreationActivity extends AppCompatActivity implements PrivateChatCreationPresenter.View {
    public static String GROUP_CHAT_ID = "GROUP_CHAT_ID";
    public static String STRANGER_CHAT_ID = "STRANGER_CHAT_ID";

    private ContactAdapter adapter;

    private PrivateChatCreationPresenter presenter;

    private User stranger = new User(STRANGER_CHAT_ID, "Chat With Stranger", "");
    private User groupChat = new User(GROUP_CHAT_ID, "Create Group Chat", "");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setVisibility(View.GONE);

        setTitle("Create New Chat");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        adapter = new ContactAdapter(this, position -> {
            User user = adapter.getData().get(position);
            if (user.getId().equals(GROUP_CHAT_ID)) {
                startActivity(new Intent(this, GroupChatCreationActivity.class));
            } else if (user.getId().equals(STRANGER_CHAT_ID)) {
                ChatWithStrangerDialogFragment dialogFragment = new ChatWithStrangerDialogFragment();
                dialogFragment.show(getFragmentManager(), "show_group_name");
            } else {
                ContactDialogProfileFragment dialogFragment = ContactDialogProfileFragment.newInstance(user);
                dialogFragment.show(getFragmentManager(), "ea");
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAlumni);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        presenter = new PrivateChatCreationPresenter(this, SampleApp.getInstance().getComponent().getUserRepository());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loadContacts();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
            finish();
        }
        return true;
    }

    @Override
    public void showContacts(List<User> contacts) {
        contacts.add(stranger);
        contacts.add(groupChat);
        adapter.clear();
        adapter.add(contacts);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
