package com.arnacon.java_chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnacon.chat_library.ChatManager;
import com.arnacon.chat_library.DisplayedMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity implements ChatManager.ChatUpdateListener {

    private static final int IMAGE_REQUEST_CODE = 1;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageView imageView;
    private MessageAdapter messageAdapter;
    private ChatManager chatManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);

        setupUI();
        setupChatManager();
    }

    private void setupUI() {
        String username = getIntent().getStringExtra("username");
        username = username != null ? username : "user123";

        messageEditText = findViewById(R.id.edit_gchat_message);
        Button sendButton = findViewById(R.id.button_gchat_send);
        messagesRecyclerView = findViewById(R.id.recycler_gchat);
        imageView = findViewById(R.id.imageView);

        messageAdapter = new MessageAdapter(new ArrayList<>(), username, this::openFile);
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sendButton.setOnClickListener(v -> sendMessage());
        findViewById(R.id.button_send_image).setOnClickListener(v -> openImagePicker());
    }

    private void setupChatManager() {
        String username = getIntent().getStringExtra("username");
        if (username == null) username = "user123";

        chatManager = new ChatManager(this, username);
        chatManager.setUpdateListener(this);
        chatManager.loadRecentMessages(0, 10);
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString();
        if (!messageText.isEmpty()) {
            newMessage("text", messageText, null);
            messageEditText.setText("");
        }
    }

    @Override
    public void onNewMessage(DisplayedMessage displayedMessage) {
        updateMessagesList(() -> messageAdapter.addMessage(displayedMessage));
    }

    @Override
    public void onNewMessages(@NonNull List<? extends DisplayedMessage> displayedMessages) {
        updateMessagesList(() -> displayedMessages.forEach(messageAdapter::addMessage));
    }

    private void updateMessagesList(Runnable updateOperation) {
        runOnUiThread(() -> {
            updateOperation.run();
            messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private void openFile(Uri fileUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                Log.e("ChatRoomActivity", "Error opening file: " + e.toString());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data != null ? data.getData() : null;
            if (uri != null) {
                newMessage("file", "Image", uri);
            }
        }
    }

    private void newMessage(String messageType, String content, Uri uri) {
        chatManager.newMessage(messageType, content, uri,
                newMessage -> {
                    Log.d("ChatRoomActivity", "Message created successfully: " + newMessage.getContent());
                    chatManager.storeMessage(newMessage);
                    chatManager.uploadMessage(newMessage);
                    return null;
                },
                throwable -> {
                    Log.e("ChatRoomActivity", "Failed to create message", throwable);
                    return null;
                });
    }
}
