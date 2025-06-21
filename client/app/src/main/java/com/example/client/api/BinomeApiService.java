package com.example.client.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface BinomeApiService {
    
    // Authentication endpoints
    @POST("api/auth/register")
    Call<ApiModels.AuthResponse> register(@Body ApiModels.RegisterRequest request);
    
    @POST("api/auth/login")
    Call<ApiModels.AuthResponse> login(@Body ApiModels.LoginRequest request);
    
    // Profile endpoints
    @POST("api/profile")
    Call<ApiModels.Profile> createProfile(@Body ApiModels.CreateProfileRequest request);
    
    @GET("api/profile")
    Call<ApiModels.Profile> getCurrentProfile();
    
    // Matching endpoints
    @GET("api/matches/potential")
    Call<List<ApiModels.PotentialMatch>> getPotentialMatches();
    
    @POST("api/matches/request")
    Call<ApiModels.MatchResponse> sendMatchRequest(@Body ApiModels.MatchRequest request);
    
    // Messaging endpoints
    @POST("api/messages")
    Call<ApiModels.Message> sendMessage(@Body ApiModels.SendMessageRequest request);
    
    @GET("api/messages")
    Call<List<ApiModels.Message>> getConversation(@Query("user") String otherUsername);
    
    @GET("api/messages/conversations")
    Call<List<ApiModels.Conversation>> getConversations();
} 