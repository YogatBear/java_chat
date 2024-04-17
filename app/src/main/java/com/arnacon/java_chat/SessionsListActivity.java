package com.arnacon.java_chat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnacon.chat_library.ChatSession;
import com.arnacon.chat_library.Message;
import com.arnacon.chat_library.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SessionsListActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SessionAdapter sessionAdapter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        assert username != null;
        sessionManager = new SessionManager(this);

        RecyclerView sessionsRecyclerView = findViewById(R.id.sessionsRecyclerView);
        FloatingActionButton addSessionFab = findViewById(R.id.addSessionFab);

        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new SessionAdapter(this, new ArrayList<>()); // Initialize with empty list
        sessionsRecyclerView.setAdapter(sessionAdapter); // Set the adapter

        addSessionFab.setOnClickListener(view -> showNewSessionDialog());

        sessionAdapter.setOnItemClickListener(this::openChat);

        // Load initial list
        refreshSessionsList();
    }

    @Override
    protected void onResume(){
        super.onResume();
        refreshSessionsList();
    }

    private void openChat(String sessionId) {
        Intent newIntent = new Intent(SessionsListActivity.this, ChatRoomActivity.class);
        newIntent.putExtra("username", username); // Pass username if necessary
        newIntent.putExtra("sessionContext", sessionId); // Pass the clicked session ID
        startActivity(newIntent);
    }

    private void showNewSessionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Username");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> createNewSession(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createNewSession(String username) {
        Log.d("SessionsListActivity", "Creating new session with username: " + username);
        openChat(username);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        // Refresh list to reflect new message arrival
        refreshSessionsList();
    }

    private void refreshSessionsList() {
        List<ChatSession> sessions = sessionManager.loadSessions(); // Assuming getSession returns a List of ChatSessions
        sessionAdapter.updateSessions(sessions); // Update the adapter with the new list
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
