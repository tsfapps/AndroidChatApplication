package com.videocall.tsfchat.ui.groupchatcreation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.videocall.tsfchat.ui.groupchatcreation.groupinfo.GroupInfoFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupChatCreationActivity extends AppCompatActivity implements GroupChatCreationPresenter.View {
    private static final String TAG = "GroupChatCreationActivity";
    private RecyclerView selectedContactRecyclerView;
    private ContactAdapter contactAdapter;
    private SelectedContactAdapter selectedContactAdapter;
    private SearchView searchView;

    private GroupChatCreationPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setVisibility(View.GONE);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setTitle("Select Participants");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton nextFab = findViewById(R.id.nextFloatingButton);

        RecyclerView contactRecyclerView = findViewById(R.id.recyclerViewAlumni);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setHasFixedSize(true);

        contactAdapter = new ContactAdapter(this, position -> {
            presenter.selectContact(contactAdapter.getData().get(position));
        });
        contactRecyclerView.setAdapter(contactAdapter);

        selectedContactRecyclerView = findViewById(R.id.recyclerViewSelected);
        selectedContactRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedContactRecyclerView.setHasFixedSize(true);

        selectedContactAdapter = new SelectedContactAdapter(this, position -> {
            presenter.selectContact(selectedContactAdapter.getData().get(position));
        });
        selectedContactRecyclerView.setAdapter(selectedContactAdapter);

        nextFab.setOnClickListener(v -> {
            if (isFragmentOn()) {
                GroupInfoFragment currentFragment = (GroupInfoFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout);
                currentFragment.proceedCreateGroup();
            } else {
                searchView.onActionViewCollapsed();
                if (selectedContactIsMoreThanOne()) {
                    List<User> contacts = new ArrayList<>();
                    int size = selectedContactAdapter.getData().size();
                    for (int i = 0; i < size; i++) {
                        contacts.add(selectedContactAdapter.getData().get(i).getUser());
                    }

                    Fragment fr = GroupInfoFragment.newInstance(contacts);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.frameLayout, fr)
                            .addToBackStack("tag")
                            .commit();
                } else {
                    Toast.makeText(this, "select at least one", Toast.LENGTH_SHORT).show();
                }
            }
        });

        presenter = new GroupChatCreationPresenter(this, SampleApp.getInstance().getComponent().getUserRepository());
        presenter.loadContacts();

    }

    private boolean isFragmentOn() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        return !(currentFragment == null || !currentFragment.isVisible());
    }

    private boolean selectedContactIsMoreThanOne() {
        return selectedContactAdapter.getData().size() > 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_search) {
            onReturn();
        }

        return true;
    }

    private void onReturn() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (currentFragment == null || !currentFragment.isVisible()) {
            finish();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        onReturn();
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
    public void showContacts(List<SelectableUser> contacts) {
        contactAdapter.clear();
        contactAdapter.addOrUpdate(contacts);
    }

    @Override
    public void onSelectedContactChange(SelectableUser contact) {
        contactAdapter.addOrUpdate(contact);
        if (contact.isSelected()) {
            selectedContactAdapter.addOrUpdate(contact);
        } else {
            selectedContactAdapter.remove(contact);
        }

        selectedContactRecyclerView.setVisibility(selectedContactAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
