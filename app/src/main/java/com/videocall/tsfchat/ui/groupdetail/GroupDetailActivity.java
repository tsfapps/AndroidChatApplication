package com.videocall.tsfchat.ui.groupdetail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.ui.addmember.AddGroupMemberActivity;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;


public class GroupDetailActivity extends AppCompatActivity implements GroupDetailPresenter.View {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    private static final int RC_ADD_PARTICIPANTS = 3;
    protected QiscusChatRoom qiscusChatRoom;

    private EditText groupName;
    private ImageView groupAvatar;
    private ProgressDialog progressDialog;
    private MemberAdapter memberAdapter;

    private GroupDetailPresenter presenter;
    private QiscusAccount account = Qiscus.getQiscusAccount();

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        resolveChatRoom(savedInstanceState);

        if (qiscusChatRoom == null) {
            finish();
            return;
        }

        presenter = new GroupDetailPresenter(this, SampleApp.getInstance().getComponent().getChatRoomRepository());

        groupName = findViewById(R.id.group_name_input);
        groupAvatar = findViewById(R.id.group_avatar);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton addParticipantButton = findViewById(R.id.addParticipant);
        addParticipantButton.setOnClickListener(v -> addNewMember());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait!");

        memberAdapter = new MemberAdapter(this, position -> onMemberClick(memberAdapter.getData().get(position)));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memberAdapter);

        setTitle("Group Detail");

        bindRoomData();
    }

    private void onMemberClick(final QiscusRoomMember qiscusRoomMember) {
        if (!qiscusRoomMember.getEmail().equalsIgnoreCase(account.getEmail())) {
            CharSequence[] memberOptions = {"Remove"};
            new AlertDialog.Builder(this)
                    .setTitle(qiscusRoomMember.getUsername())
                    .setItems(memberOptions, (dialog, which) -> removeMember(qiscusRoomMember))
                    .setCancelable(true)
                    .create()
                    .show();
        }
    }

    private void addNewMember() {
        startActivityForResult(AddGroupMemberActivity.generateIntent(this, qiscusChatRoom), RC_ADD_PARTICIPANTS);
    }

    private void removeMember(QiscusRoomMember member) {
        presenter.removeMember(qiscusChatRoom.getId(), new User(member.getEmail(), member.getUsername(), member.getAvatar()));
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }
    }

    private void bindRoomData() {
        groupName.setText(qiscusChatRoom.getName());
        Nirmana.getInstance().get().load(qiscusChatRoom.getAvatarUrl()).into(groupAvatar);
        memberAdapter.addOrUpdate(qiscusChatRoom.getMember());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            updateRoomName();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateRoomName() {
        presenter.updateRoomName(qiscusChatRoom.getId(), groupName.getText().toString());
    }

    @Override
    public void onMemberRemoved(User user) {
        QiscusRoomMember member = new QiscusRoomMember();
        member.setEmail(user.getId());
        memberAdapter.remove(member);
        qiscusChatRoom.getMember().remove(member);
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

    @Override
    public void onRoomNameUpdated(QiscusChatRoom qiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom;
        bindRoomData();
        setIntentResult();
    }

    private void setIntentResult() {
        Intent intent = new Intent();
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_PARTICIPANTS && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                qiscusChatRoom = data.getParcelableExtra(CHAT_ROOM_DATA);
                bindRoomData();
                setIntentResult();
            }
        }
    }
}
