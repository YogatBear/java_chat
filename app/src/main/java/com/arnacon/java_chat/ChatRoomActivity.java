package com.arnacon.java_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnacon.chat_library.ChatManager;
import com.arnacon.chat_library.ChatSession;
import com.arnacon.chat_library.DisplayedMessage;
import com.arnacon.chat_library.Message;
import com.arnacon.chat_library.SessionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import android.widget.Button;
public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private MessageAdapter messageAdapter;
    private String sessionContext;
    private String username;
    private ChatManager chatManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        setupIntentData();

        SessionManager sessionManager = new SessionManager(this, username);
        chatManager = new ChatManager(this,username);

        Log.d("ChatRoomActivity", "Fetching session: " + sessionContext);

        ChatSession session = sessionManager.getSession(sessionContext);

        setupUI();
    }

    private void setupIntentData() {
        Intent intent = getIntent();
        sessionContext = intent.getStringExtra("sessionContext"); // Retrieve the session ID
        username = intent.getStringExtra("username");
    }

    private void setupUI() {
        messagesRecyclerView = findViewById(R.id.recycler_gchat); // Use your actual RecyclerView ID
        messageEditText = findViewById(R.id.edit_gchat_message); // And EditText ID for message input
        Button sendButton = findViewById(R.id.button_gchat_send); // And the Button ID for sending a message

        messageAdapter = new MessageAdapter(new ArrayList<>(), username, null); // Replace "YourUsername" appropriately
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView imageView = findViewById(R.id.imageView);
        findViewById(R.id.button_send_image).setOnClickListener(v -> openImagePicker());

        sendButton.setOnClickListener(v -> sendMessage());

        List<Message> recentMessages = chatManager.loadRecentMessages(0, 10, sessionContext);
        for (Message message : recentMessages) {;
            displayMessage(message);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        // Check if the message belongs to the current session
        if (event.message.getContext().equals(sessionContext)) {
            // Update your adapter here
            displayMessage(event.message);
        }
    }

    private Message newMessage(String messageType, String content, Uri uri) {
        // Initialize the newMessage as null to ensure method returns a value in all cases
        Message newMessage = null;
        CompletableFuture<Message> futureMessage = chatManager.newMessage(messageType, content, uri, username);
        try {
            newMessage = futureMessage.get(); // This blocks the current thread.
            Log.d("ChatRoomActivity", "Message created successfully: " + newMessage.getContent());
            chatManager.storeMessage(newMessage, sessionContext);
            chatManager.uploadMessage(newMessage, sessionContext);
        } catch (InterruptedException e) {
            // Handle the InterruptedException
            Thread.currentThread().interrupt();
            Log.e("ChatRoomActivity", "Message creation was interrupted", e);
        } catch (ExecutionException e) {
            // Handle the ExecutionException
            Log.e("ChatRoomActivity", "Exception during message creation", e.getCause());
        }
        return newMessage;
    }

    private void displayMessage(Message message) {
        DisplayedMessage displayedMessage = chatManager.getDisplayedMessage(message);
        Log.d("ChatRoomActivity", "Displaying message:" + displayedMessage);
        messageAdapter.addMessage(displayedMessage);
        messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }


    private void sendMessage() {
        String messageText = messageEditText.getText().toString();
        if (!messageText.isEmpty()) {
            messageEditText.setText("");
            Message message = newMessage("text", messageText, null);
            displayMessage(message);
        }
    }

    private void openFile(Uri fileUri) {
        CompletableFuture.supplyAsync(() -> newMessage("file", "", fileUri))
                .thenAcceptAsync(this::displayMessage, runOnUiThreadExecutor());
    }

    private Executor runOnUiThreadExecutor() {
        return this::runOnUiThread;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            openFile(imageUri);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
