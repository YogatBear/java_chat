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
    private static MessageListener instance = null;
    private boolean alreadyListening = false;
    private final FirestoreMessaging firestoreMessaging;
    private final ChatManager chatManager;
    private final String username;


    private MessageListener(Context context, String user) {
        // Assuming FirestoreMessaging takes the current user as the parameter for initialization
        this.firestoreMessaging = new FirestoreMessaging(user);
        this.chatManager = new ChatManager(context, user);
        this.username = user;
    }

    public static synchronized MessageListener getInstance(Context context, String user) {
        if (instance == null || !instance.username.equals(user)) {
            instance = new MessageListener(context, user);
        }
        return instance;
    }

    public void startListening() {
        if (!alreadyListening) {
            alreadyListening = true;
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
}
