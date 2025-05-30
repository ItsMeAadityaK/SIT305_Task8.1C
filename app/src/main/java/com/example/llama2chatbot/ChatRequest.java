package com.example.llama2chatbot;

import java.util.List;

public class ChatRequest {
    private String userMessage;
    private List<ChatPair> chatHistory;

    public ChatRequest(String userMessage, List<ChatPair> chatHistory) {
        this.userMessage = userMessage;
        this.chatHistory = chatHistory;
    }

    public static class ChatPair {
        private String User;
        private String Llama;

        public ChatPair(String user, String llama) {
            this.User = user;
            this.Llama = llama;
        }
    }
}
