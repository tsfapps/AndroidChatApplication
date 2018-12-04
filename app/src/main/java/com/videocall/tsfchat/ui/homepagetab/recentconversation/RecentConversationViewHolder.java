package com.videocall.tsfchat.ui.homepagetab.recentconversation;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.videocall.tsfchat.R;
import com.videocall.tsfchat.ui.common.OnItemClickListener;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created on : May 16, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class RecentConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView itemName;
    private ImageView picture;
    private TextView lastMessage;
    private TextView lastMessageTime;
    private TextView unreadCounter;
    private FrameLayout unreadFrame;

    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat dateFormatToday = new SimpleDateFormat("hh:mm a");

    public RecentConversationViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);
        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);

        itemName = itemView.findViewById(R.id.textViewRoomName);
        lastMessage = itemView.findViewById(R.id.textViewLastMessage);
        picture = itemView.findViewById(R.id.imageViewRoomAvatar);
        lastMessageTime = itemView.findViewById(R.id.textViewRoomTime);
        unreadCounter = itemView.findViewById(R.id.unreadCounterView);
        unreadFrame = itemView.findViewById(R.id.unreadCounterFrame);
    }

    public void bind(QiscusChatRoom chatRoom) {
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .dontAnimate())
                .load(chatRoom.getAvatarUrl())
                .into(picture);
        itemName.setText(chatRoom.getName());
        int unreadCount = chatRoom.getUnreadCount();
        if (unreadCount > 0) {
            unreadCounter.setText(String.valueOf(unreadCount));
            unreadFrame.setVisibility(View.VISIBLE);
        } else {
            unreadFrame.setVisibility(View.GONE);
        }
        bindLastComment(chatRoom.getLastComment());
    }

    private void bindLastComment(QiscusComment comment) {
        if (comment == null) {
            lastMessage.setText("");
            lastMessageTime.setText("");
            return;
        }

        String messageText = comment.isMyComment() ? "You: " : comment.getSender().split(" ")[0] + ": ";
        switch (comment.getType()) {
            case IMAGE:
                messageText += "\uD83D\uDCF7 " + (TextUtils.isEmpty(comment.getCaption()) ?
                        QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_send_a_photo) : comment.getCaption());
                break;
            case VIDEO:
                messageText += "\uD83C\uDFA5 " + (TextUtils.isEmpty(comment.getCaption()) ?
                        QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_send_a_video) : comment.getCaption());
                break;
            case AUDIO:
                messageText += "\uD83D\uDD0A " + QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_send_a_audio);
                break;
            case CONTACT:
                messageText += "\u260E " + QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_contact) + ": " +
                        comment.getContact().getName();
                break;
            case LOCATION:
                messageText += "\uD83D\uDCCD " + comment.getMessage();
                break;
            case CAROUSEL:
                try {
                    JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                    JSONArray cards = payload.optJSONArray("cards");
                    if (cards.length() > 0) {
                        messageText += "\uD83D\uDCDA " + cards.optJSONObject(0).optString("title");
                    } else {
                        messageText += "\uD83D\uDCDA " + QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_send_a_carousel);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    messageText += "\uD83D\uDCDA " + QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_send_a_carousel);
                }
                break;
            default:
                messageText += comment.isAttachment() ? "\uD83D\uDCC4 " +
                        QiscusTextUtil.getString(com.qiscus.sdk.R.string.qiscus_send_attachment) : comment.getMessage();
                break;
        }

        lastMessage.setText(messageText);
        if (DateUtils.isToday(comment.getTime().getTime())) {
            lastMessageTime.setText(dateFormatToday.format(comment.getTime()));
        } else {
            lastMessageTime.setText(dateFormat.format(comment.getTime()));
        }
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
