package com.arnacon.java_chat;

import com.arnacon.chat_library.Message;

public class MessageEvent {

    public final Message message;

    public MessageEvent(Message message) {
        this.message = message;
    }
}

