package com.arnacon.java_chat;

import android.content.Context;
import android.se.omapi.Session;
import android.util.Log;

import com.arnacon.chat_library.ChatManager;
import com.arnacon.chat_library.ChatSession;
import com.arnacon.chat_library.DisplayedMessage;
import com.arnacon.chat_library.FirestoreMessaging;
import com.arnacon.chat_library.Message;
import com.arnacon.chat_library.PubSub;
import com.arnacon.chat_library.SessionManager;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.CompletableFuture;

public class MessageListener {
    private final FirestoreMessaging firestoreMessaging;
    private final ChatManager chatManager;


    public MessageListener(Context context, String user) {
        // Assuming FirestoreMessaging takes the current user as the parameter for initialization
        this.firestoreMessaging = new FirestoreMessaging(user);
        this.chatManager = new ChatManager(context, user);
    }

    public void startListening() {
        firestoreMessaging.listenForNewMessages(newMessage -> {
            CompletableFuture<Void> future = chatManager.storeMessage(newMessage, newMessage.getContext());
            future.thenRun(() -> EventBus.getDefault().post(new MessageEvent(newMessage))).exceptionally(ex -> {
                Log.e("MessageListener", "Error processing message", ex);
                return null;
            });
            return null;
        });
    }
}
