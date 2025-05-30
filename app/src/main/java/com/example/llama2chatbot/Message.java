package com.example.llama2chatbot;

public class Message {
    private final String content;
    private final boolean fromUser;

    public Message(String content, boolean fromUser) {
        this.content = content;
        this.fromUser = fromUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isFromUser() {
        return fromUser;
    }
}
