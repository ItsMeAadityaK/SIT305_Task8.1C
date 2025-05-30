package com.example.llama2chatbot;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private static final String ARG_USERNAME = "username";
    private List<Message> messageList;
    private MessageAdapter adapter;
    private EditText inputMessage;

    public static ChatFragment newInstance(String username) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.chatRecyclerView);
        inputMessage = view.findViewById(R.id.editTextMessage);
        ImageButton sendButton = view.findViewById(R.id.buttonSend);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        messageList.add(new Message("Welcome " + getArguments().getString(ARG_USERNAME) + "!", false));
        adapter.notifyItemInserted(messageList.size() - 1);

        sendButton.setOnClickListener(v -> {
            String userMsg = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(userMsg)) {
                addMessage(userMsg, true);
                inputMessage.setText("");
                sendToLlamaAPI(userMsg);
            }
        });

        return view;
    }

    private void addMessage(String text, boolean fromUser) {
        messageList.add(new Message(text, fromUser));
        adapter.notifyItemInserted(messageList.size() - 1);
    }

    private void sendToLlamaAPI(String userMessage) {
        List<ChatRequest.ChatPair> history = new ArrayList<>();
        for (Message msg : messageList) {
            if (!msg.isFromUser()) continue;
            history.add(new ChatRequest.ChatPair(msg.getContent(), "Waiting..."));
        }

        ChatRequest request = new ChatRequest(userMessage, history);
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getChatResponse(request).enqueue(new Callback<BotResponse>() {
            @Override
            public void onResponse(Call<BotResponse> call, Response<BotResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String rawReply = response.body().getMessage();

                    rawReply = rawReply.replaceAll("(?i)^```(?:json)?", "")
                            .replaceAll("```$", "")
                            .trim();

                    rawReply = rawReply.replaceAll("(?s)\\{\\s*\"chat_history\".*?\\}\\s*", "").trim();

                    addMessage(rawReply, false);
                } else {
                    addMessage("Oops! Llama didn't reply properly.", false);
                }
            }


            @Override
            public void onFailure(Call<BotResponse> call, Throwable t) {
                addMessage("Error: " + t.getMessage(), false);
            }
        });
    }
}
