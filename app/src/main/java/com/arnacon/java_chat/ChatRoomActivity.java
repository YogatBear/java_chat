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
import java.util.List;


public class ChatRoomActivity extends AppCompatActivity implements ChatManager.ChatUpdateListener {

    private static final int imageRequestCode = 1;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private MessageAdapter messageAdapter;
    private ChatManager chatManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview); // Assuming this is your layout file

        String username = getIntent().getStringExtra("username");
        if (username == null) username = "user123";

        messageEditText = findViewById(R.id.edit_gchat_message);
        sendButton = findViewById(R.id.button_gchat_send);
        messagesRecyclerView = findViewById(R.id.recycler_gchat);

        messageAdapter = new MessageAdapter(new java.util.ArrayList<>(), username, this::openFile);
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatManager = new ChatManager(this, username);
        chatManager.setUpdateListener(this);

        chatManager.loadRecentMessages(0,10);

        sendButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString();
            if (!messageText.isEmpty()) {
                // Replace with appropriate threading/logic to offload IO operations
                newMessage("text", messageText, null);
                messageEditText.setText("");
            }
        });

        Button sendImageButton = findViewById(R.id.button_send_image);
        sendImageButton.setOnClickListener(v -> openImagePicker());
    }

    @Override
    public void onNewMessage(DisplayedMessage displayedMessage) {
        runOnUiThread(() -> {
            messageAdapter.addMessage(displayedMessage);
            messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    @Override
    public void onNewMessages(@NonNull List<? extends DisplayedMessage> displayedMessages) {
        runOnUiThread(() -> {
            for (DisplayedMessage displayedMessage : displayedMessages) {
                messageAdapter.addMessage(displayedMessage);
            }
            messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, imageRequestCode);
    }

    private void openFile(Uri fileUri) {
        // Replace with appropriate threading/logic to offload IO operations
        new Thread(() -> {
            try {
                Uri uri = fileUri;
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                runOnUiThread(() -> {
                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);
                });
            } catch (Exception e) {
                Log.e("MainActivity", "Error opening file: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == imageRequestCode && resultCode == RESULT_OK) {
            Uri uri = data != null ? data.getData() : null;
            if (uri != null) {
                newMessage("file", "Image", uri);
            }
        }
    }

    private void newMessage(String messageType, String content, Uri uri){
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
                }
        );
    }

}
