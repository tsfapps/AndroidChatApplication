package com.videocall.tsfchat.ui.privatechatcreation;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.data.repository.ChatRoomRepository;
import com.videocall.tsfchat.ui.homepagetab.HomePageTabActivity;
import com.videocall.tsfchat.util.ChatRoomNavigator;

/**
 * Created by omayib on 05/11/17.
 */
public class ChatWithStrangerDialogFragment extends DialogFragment {
    private EditText editText;

    private ChatRoomRepository chatRoomRepository;

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_stranger_name, container, false);
        getDialog().setTitle("Stranger name");
        editText = rootView.findViewById(R.id.editText);

        chatRoomRepository = SampleApp.getInstance().getComponent().getChatRoomRepository();

        Button buttonOk = rootView.findViewById(R.id.confirm_button);
        Button buttonCancel = rootView.findViewById(R.id.cancel_button);

        buttonCancel.setOnClickListener(view -> getDialog().dismiss());

        buttonOk.setOnClickListener(view -> {
            if (!editText.getText().toString().isEmpty()) {
                chatRoomRepository.createChatRoom(new User(editText.getText().toString(), "", ""),
                        qiscusChatRoom -> {
                            ChatRoomNavigator.openChatRoom(getActivity(), qiscusChatRoom)
                                    .withParentClass(HomePageTabActivity.class)
                                    .start();
                            dismiss();
                        },
                        Throwable::printStackTrace);
                dismiss();
            }
        });
        return rootView;
    }
}
