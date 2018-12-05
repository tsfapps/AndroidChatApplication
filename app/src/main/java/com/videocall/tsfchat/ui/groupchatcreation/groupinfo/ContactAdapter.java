package com.videocall.tsfchat.ui.groupchatcreation.groupinfo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.ui.common.OnItemClickListener;
import com.videocall.tsfchat.ui.common.SortedRecyclerViewAdapter;


public class ContactAdapter extends SortedRecyclerViewAdapter<User, ContactViewHolder> {
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ContactAdapter(Context context, OnItemClickListener onItemClickListener) {
        super();
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    protected Class<User> getItemClass() {
        return User.class;
    }

    @Override
    protected int compare(User item1, User item2) {
        return item1.getName().compareTo(item2.getName());
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false), onItemClickListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getData().get(position));
    }
}
