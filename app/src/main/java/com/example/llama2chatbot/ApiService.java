package com.example.llama2chatbot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("chat")
    Call<BotResponse> getChatResponse(@Body ChatRequest request);
}
