package com.example.client.api;

import android.content.Context;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockApiService implements BinomeApiService {
    
    private Context context;
    
    public MockApiService(Context context) {
        this.context = context;
    }
    
    @Override
    public Call<ApiModels.AuthResponse> register(ApiModels.RegisterRequest request) {
        return new MockCall<>(new ApiModels.AuthResponse() {{
            setToken("mock_token_" + System.currentTimeMillis());
        }});
    }
    
    @Override
    public Call<ApiModels.AuthResponse> login(ApiModels.LoginRequest request) {
        return new MockCall<>(new ApiModels.AuthResponse() {{
            setToken("mock_token_" + System.currentTimeMillis());
        }});
    }
    
    @Override
    public Call<ApiModels.Profile> createProfile(ApiModels.CreateProfileRequest request) {
        ApiModels.Profile profile = new ApiModels.Profile();
        profile.setId(1L);
        
        // Create user object
        ApiModels.User user = new ApiModels.User();
        user.setUsername(ApiClient.getInstance(context).getUsername());
        profile.setUser(user);
        
        profile.setFormation(request.getFormation());
        profile.setSkills(request.getSkills());
        profile.setPreferences(request.getPreferences());
        return new MockCall<>(profile);
    }
    
    @Override
    public Call<ApiModels.Profile> getCurrentProfile() {
        ApiModels.Profile profile = new ApiModels.Profile();
        profile.setId(1L);
        
        // Create user object
        ApiModels.User user = new ApiModels.User();
        user.setUsername(ApiClient.getInstance(context).getUsername());
        profile.setUser(user);
        
        profile.setFormation("Computer Science");
        
        Set<String> skills = new HashSet<>();
        skills.add("Java");
        skills.add("Android");
        skills.add("Spring Boot");
        profile.setSkills(skills);
        profile.setPreferences("Looking for study partners in mobile development");
        
        return new MockCall<>(profile);
    }
    
    @Override
    public Call<List<ApiModels.PotentialMatch>> getPotentialMatches() {
        List<ApiModels.PotentialMatch> matches = new ArrayList<>();
        
        // Create mock matches
        for (int i = 1; i <= 5; i++) {
            ApiModels.PotentialMatch match = new ApiModels.PotentialMatch();
            match.setUserId((long) i);
            match.setUsername("Student" + i);
            match.setFormation("Computer Science");
            
            Set<String> commonSkills = new HashSet<>();
            commonSkills.add("Java");
            commonSkills.add("Android");
            match.setCommonSkills(commonSkills);
            match.setCompatibilityScore(85 + i * 2);
            
            matches.add(match);
        }
        
        return new MockCall<>(matches);
    }
    
    @Override
    public Call<ApiModels.MatchResponse> sendMatchRequest(ApiModels.MatchRequest request) {
        ApiModels.MatchResponse response = new ApiModels.MatchResponse();
        if ("LIKE".equals(request.getAction())) {
            response.setMatch(Math.random() < 0.3); // 30% chance of match
            response.setMessage(response.isMatch() ? "It's a match!" : "Like sent successfully");
        } else {
            response.setMatch(false);
            response.setMessage("Passed");
        }
        return new MockCall<>(response);
    }
    
    @Override
    public Call<ApiModels.Message> sendMessage(ApiModels.SendMessageRequest request) {
        ApiModels.Message message = new ApiModels.Message();
        message.setId(System.currentTimeMillis());
        message.setSender(ApiClient.getInstance(context).getUsername());
        message.setRecipient(request.getRecipient());
        message.setContent(request.getContent());
        message.setTimestamp(java.time.Instant.now().toString());
        return new MockCall<>(message);
    }
    
    private Call<List<ApiModels.Message>> getConversation(String otherUsername) {
        List<ApiModels.Message> messages = new ArrayList<>();
        
        // Create mock conversation
        ApiModels.Message msg1 = new ApiModels.Message();
        msg1.setId(1L);
        msg1.setSender(otherUsername);
        msg1.setRecipient(ApiClient.getInstance(context).getUsername());
        msg1.setContent("Hey! I saw we're both studying Computer Science. Want to study together?");
        msg1.setTimestamp(java.time.Instant.now().minusSeconds(3600).toString());
        
        ApiModels.Message msg2 = new ApiModels.Message();
        msg2.setId(2L);
        msg2.setSender(ApiClient.getInstance(context).getUsername());
        msg2.setRecipient(otherUsername);
        msg2.setContent("Sure! That sounds great. What topics are you working on?");
        msg2.setTimestamp(java.time.Instant.now().minusSeconds(1800).toString());
        
        messages.add(msg1);
        messages.add(msg2);
        
        return new MockCall<>(messages);
    }
    
    @Override
    public Call<ApiModels.Profile> getProfile() {
        return getCurrentProfile();
    }
    
    @Override
    public Call<ApiModels.Profile> updateProfile(ApiModels.CreateProfileRequest request) {
        return createProfile(request);
    }
    
    @Override
    public Call<ApiModels.MatchResponse> sendMatchAction(ApiModels.MatchRequest request) {
        return sendMatchRequest(request);
    }
    
    @Override
    public Call<List<ApiModels.Conversation>> getConversations() {
        List<ApiModels.Conversation> conversations = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            ApiModels.Conversation conv = new ApiModels.Conversation();
            conv.setUsername("student" + i);
            conv.setDisplayName("Student " + i);
            conv.setLastMessage("Last message from conversation " + i);
            conv.setTimestamp(java.time.Instant.now().minusSeconds(i * 1800).toString());
            conv.setHasUnreadMessages(i == 1);
            conv.setUnreadCount(i == 1 ? 2 : 0);
            conversations.add(conv);
        }
        
        return new MockCall<>(conversations);
    }
    
    @Override
    public Call<List<ApiModels.Message>> getConversationMessages(String otherUsername) {
        return getConversation(otherUsername);
    }
    
    @Override
    public Call<Void> markConversationAsRead(String otherUsername) {
        return new MockCall<>(null);
    }
    
    @Override
    public Call<ApiModels.Message> sendMessageLegacy(ApiModels.SendMessageRequest request) {
        return sendMessage(request);
    }
    
    @Override
    public Call<List<ApiModels.Message>> getConversationLegacy(String otherUsername) {
        return getConversation(otherUsername);
    }
    
    // Mock Call implementation
    private static class MockCall<T> implements Call<T> {
        private final T data;
        private boolean executed = false;
        
        public MockCall(T data) {
            this.data = data;
        }
        
        @Override
        public Response<T> execute() {
            executed = true;
            return Response.success(data);
        }
        
        @Override
        public void enqueue(Callback<T> callback) {
            // Simulate network delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                executed = true;
                callback.onResponse(this, Response.success(data));
            }, 500); // 500ms delay
        }
        
        @Override
        public boolean isExecuted() {
            return executed;
        }
        
        @Override
        public void cancel() {
            // No-op for mock
        }
        
        @Override
        public boolean isCanceled() {
            return false;
        }
        
        @Override
        public Call<T> clone() {
            return new MockCall<>(data);
        }
        
        @Override
        public okhttp3.Request request() {
            return null;
        }
        
        @Override
        public okio.Timeout timeout() {
            return okio.Timeout.NONE;
        }
    }
} 