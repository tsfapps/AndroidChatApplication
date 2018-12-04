package com.videocall.tsfchat.ui.homepagetab.contact;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.ui.privatechatcreation.ContactDialogProfileFragment;

import java.util.List;

/**
 * Created by asyrof on 17/11/17.
 */
public class ContactFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ContactPresenter.View {
    private static final String TAG = "PrivateChatCreationActivity";
    private ContactAdapter adapter;
    private LinearLayout emptyRoomVIew;
    private SwipeRefreshLayout swipeContactRefreshLayout;

    private ContactPresenter presenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        View v = getView();

        emptyRoomVIew = v.findViewById(R.id.empty_contact_view);
        swipeContactRefreshLayout = v.findViewById(R.id.swipeContactRefreshLayout);

        adapter = new ContactAdapter(getActivity(), position -> {
            ContactDialogProfileFragment dialogFragment = ContactDialogProfileFragment.newInstance(adapter.getData().get(position));
            dialogFragment.show(getActivity().getFragmentManager(), TAG);
        });
        RecyclerView recyclerView = v.findViewById(R.id.recyclerViewAlumni);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        swipeContactRefreshLayout.setOnRefreshListener(this);

        presenter = new ContactPresenter(this, SampleApp.getInstance().getComponent().getUserRepository());
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadContacts();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchView.OnCloseListener closeListener = () -> {
            int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
            Toast.makeText(getActivity().getBaseContext(), String.valueOf(count), Toast.LENGTH_SHORT).show();
            if (count != 0) {
                getActivity().getSupportFragmentManager().popBackStack();
            }

            return true;
        };

        searchView.setOnCloseListener(closeListener);
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        presenter.loadContacts();
    }

    @Override
    public void showContacts(List<User> contacts) {
        adapter.clear();
        adapter.add(contacts);
        emptyRoomVIew.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        swipeContactRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        swipeContactRefreshLayout.setRefreshing(false);
    }
}