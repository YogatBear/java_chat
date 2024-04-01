package com.arnacon.java_chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.arnacon.chat_library.Storage;

public class UsernameActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityusername);

        EditText editTextUsername = findViewById(R.id.editTextUsername);
        Button buttonSubmit = findViewById(R.id.buttonSubmitUsername);
        Button buttonDeleteDatabase = findViewById(R.id.buttonDeleteDatabase);

        buttonSubmit.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            if (!username.isEmpty()) {
                Intent intent = new Intent(UsernameActivity.this, com.arnacon.java_chat.ChatRoomActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish(); // Close this activity
            }
        });

        buttonDeleteDatabase.setOnClickListener(v -> {
            Storage storage = new Storage(UsernameActivity.this);
            storage.deleteDatabase(UsernameActivity.this);
        });
    }
}
