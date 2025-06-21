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
    
    @PUT("api/profile")
    Call<ApiModels.Profile> updateProfile(@Body ApiModels.CreateProfileRequest request);
    
    @GET("api/profile")
    Call<ApiModels.Profile> getProfile();
    
    @GET("api/profile/current")
    Call<ApiModels.Profile> getCurrentProfile();
    
    // Matching endpoints
    @GET("api/matches/potential")
    Call<List<ApiModels.PotentialMatch>> getPotentialMatches();
    
    @POST("api/matches/request")
    Call<ApiModels.MatchResponse> sendMatchRequest(@Body ApiModels.MatchRequest request);
    
    @POST("api/matches/action")
    Call<ApiModels.MatchResponse> sendMatchAction(@Body ApiModels.MatchRequest request);
    
    // Messaging endpoints
    @GET("api/messages/conversations")
    Call<List<ApiModels.Conversation>> getConversations();
    
    @GET("api/messages/conversation/{otherUsername}")
    Call<List<ApiModels.Message>> getConversationMessages(@Path("otherUsername") String otherUsername);
    
    @POST("api/messages/send")
    Call<ApiModels.Message> sendMessage(@Body ApiModels.SendMessageRequest request);
    
    @POST("api/messages/conversation/{otherUsername}/read")
    Call<Void> markConversationAsRead(@Path("otherUsername") String otherUsername);
    
    // Legacy messaging endpoints for backward compatibility
    @POST("api/messages")
    Call<ApiModels.Message> sendMessageLegacy(@Body ApiModels.SendMessageRequest request);
    
    @GET("api/messages")
    Call<List<ApiModels.Message>> getConversationLegacy(@Query("user") String otherUsername);
} 