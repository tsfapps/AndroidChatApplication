package com.videocall.tsfchat.ui.groupchatroom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.videocall.tsfchat.ui.groupdetail.GroupDetailActivity;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.ui.QiscusChannelActivity;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.ui.QiscusGroupChatActivity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GroupChatRoomActivity extends QiscusGroupChatActivity {
    private static final int RC_MODIFY_GROUP = 123;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        return generateIntent(context, qiscusChatRoom, null, null,
                false, null, null);
    }

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom,
                                        String startingMessage, List<File> shareFiles,
                                        boolean autoSendExtra, List<QiscusComment> comments,
                                        QiscusComment scrollToComment) {
        if (!qiscusChatRoom.isGroup()) {
            return QiscusChatActivity.generateIntent(context, qiscusChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        if (qiscusChatRoom.isChannel()) {
            return QiscusChannelActivity.generateIntent(context, qiscusChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        Intent intent = new Intent(context, GroupChatRoomActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        intent.putExtra(EXTRA_STARTING_MESSAGE, startingMessage);
        intent.putExtra(EXTRA_SHARING_FILES, (Serializable) shareFiles);
        intent.putExtra(EXTRA_AUTO_SEND, autoSendExtra);
        intent.putParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS, (ArrayList<QiscusComment>) comments);
        intent.putExtra(EXTRA_SCROLL_TO_COMMENT, scrollToComment);
        return intent;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        super.onViewReady(savedInstanceState);
        toolbar.setOnClickListener(v -> startActivityForResult(GroupDetailActivity.generateIntent(GroupChatRoomActivity.this, qiscusChatRoom),
                RC_MODIFY_GROUP));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_MODIFY_GROUP && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                qiscusChatRoom = data.getParcelableExtra(CHAT_ROOM_DATA);
                binRoomData();
            }
        }
    }
}
