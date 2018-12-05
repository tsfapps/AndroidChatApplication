package com.videocall.tsfchat.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.videocall.tsfchat.R;
import com.videocall.tsfchat.SampleApp;
import com.videocall.tsfchat.data.model.User;
import com.videocall.tsfchat.ui.login.LoginActivity;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity implements ProfilePresenter.View {

    private static final int REQUEST_CODE_TAKE_PICTURE = 11;
    private static final int REQUEST_CODE_CHOOSE_PICTURE = 12;
    private TextView displayName;
    private TextView userId;
    private ImageView picture;

    private ProfilePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        picture = findViewById(R.id.single_avatar);
        displayName = findViewById(R.id.profile_display_name);
        userId = findViewById(R.id.profile_user_id);

        presenter = new ProfilePresenter(this, SampleApp.getInstance().getComponent().getUserRepository());
        presenter.loadUser();

        findViewById(R.id.upload_icon).setOnClickListener(this::showPopupMenu);
        findViewById(R.id.logout_text).setOnClickListener(view -> presenter.logout());
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_upload, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.take_picture) {
                takePicture();
            } else if (item.getItemId() == R.id.choose_picture) {
                choosePicture();
            }
            return true;
        });
        popupMenu.show();
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = QiscusImageUtil.createImageFile();
            } catch (IOException ex) {
                showError("Failed to write temporary picture!");
            }

            if (photoFile != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(this, Qiscus.getProviderAuthorities(), photoFile));
                }
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
            }
        }
    }

    @Override
    public void showUser(User user) {
        Nirmana.getInstance().get().load(user.getAvatarUrl()).into(picture);
        displayName.setText(user.getName());
        userId.setText(user.getId());
    }

    @Override
    public void showLoginPage() {
        startActivity(
                new Intent(ProfileActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        );
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri processedPhoto = (Uri.parse(QiscusCacheManager.getInstance().getLastImagePath()));
                String realPathFromURI = QiscusFileUtil.getRealPathFromURI(processedPhoto);
                presenter.uploadPhoto(realPathFromURI);
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                String imagePath = QiscusFileUtil.getRealPathFromURI(selectedImage);
                presenter.uploadPhoto(imagePath);
            }
        }
    }
}
